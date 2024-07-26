package Backend.BangWool.member.service;

import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.LocalSignUpRequest;
import Backend.BangWool.member.dto.OAuthSignUpRequest;
import Backend.BangWool.member.repository.MemberRepository;
import Backend.BangWool.util.CONSTANT;
import Backend.BangWool.util.RedisUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class SignUpServiceTest {

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private RedisUtil redisUtill;

    @MockBean
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private SignUpService signUpService;

    private static Stream<Arguments> invalidLocalSignUp() {
        return Stream.of(
                Arguments.of("test1234!!", "test1234!!", "닉네임", true, false, "true"), // 이메일 중복
                Arguments.of("test1234!!", "different", "닉네임", false, false, "true"), // 비밀번호 틀림
                Arguments.of("test!!", "test!!", "닉네임", false, false, "true"), // 비밀번호 숫자 X
                Arguments.of("test0!", "test0!", "닉네임", false, false, "true"), // 비밀번호 8자 미만
                Arguments.of("test1234!!", "test1234!!", "닉네임", false, true, "true"), // 닉네임 중복
                Arguments.of("test1234!!", "test1234!!", "!$@", false, false, "true"), // 닉네임 특수문자
                Arguments.of("test1234!!", "test1234!!", "닉네임1234567890", false, false, "true"), // 닉네임 10자 초과
                Arguments.of("test1234!!", "test1234!!", "닉네임", false, false, null) // 이메일 확인 안됨
        );
    }

    @DisplayName("자체 회원가입 실패")
    @ParameterizedTest
    @MethodSource("invalidLocalSignUp")
    void localSignUpFail(String pw1, String pw2, String nickname,
                         boolean isEmailExist, boolean isNicknameExist, String getVerifyData) {
        // given
        LocalSignUpRequest request = LocalSignUpRequest.builder()
                .email("test@test.com")
                .password1(pw1)
                .password2(pw2)
                .name("홍길동")
                .nickname(nickname)
                .birth(LocalDate.of(2000, 10, 12))
                .build();

        // mocking
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(isEmailExist);
        when(memberRepository.existsByNickname(request.getNickname())).thenReturn(isNicknameExist);
        if (getVerifyData == null) {
            when(redisUtill.getData(CONSTANT.REDIS_EMAIL_VERIFY + request.getEmail())).thenReturn(Optional.empty());
        } else {
            when(redisUtill.getData(CONSTANT.REDIS_EMAIL_VERIFY + request.getEmail())).thenReturn(Optional.of(getVerifyData));
        }


        // when & then
        assertThrows(BadRequestException.class, () -> signUpService.localSignUp(request));

    }


    @DisplayName("자체 회원가입 성공")
    @Test
    void localSignUp() {
        // given
        LocalSignUpRequest request = LocalSignUpRequest.builder()
                .email("test@test.com")
                .password1("test1234!!")
                .password2("test1234!!")
                .name("홍길동")
                .nickname("테스트")
                .birth(LocalDate.of(2000, 10, 12))
                .build();

        // mocking
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(memberRepository.existsByNickname(request.getNickname())).thenReturn(false);
        when(redisUtill.getData(CONSTANT.REDIS_EMAIL_VERIFY + request.getEmail())).thenReturn(Optional.of("true"));

        // when
        signUpService.localSignUp(request);

        // then
        verify(memberRepository, times(1)).save(any(MemberEntity.class));
        verify(redisUtill, times(2)).deleteData(anyString());

    }


    private static Stream<Arguments> invalidSocial() {
        return Stream.of(
                Arguments.of(null, null, false, false),
                Arguments.of("google", "kakao", true, false),
                Arguments.of("google", "kakao", false, true)
        );
    }

    @DisplayName("소셜 회원가입 실패")
    @ParameterizedTest
    @MethodSource("invalidSocial")
    void socialSignUpFail(String google, String kakao, boolean googleExist, boolean kakaoExist) {
        // given
        OAuthSignUpRequest request = OAuthSignUpRequest.builder()
                .email("test@test.com")
                .name("홍길동")
                .nickname("닉네임")
                .birth(LocalDate.of(2000, 10, 12))
                .googleId(google)
                .kakaoId(kakao)
                .build();

        // mocking
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(memberRepository.existsByNickname(request.getNickname())).thenReturn(false);
        when(memberRepository.existsByGoogleId(request.getGoogleId())).thenReturn(googleExist);
        when(memberRepository.existsByKakaoId(request.getKakaoId())).thenReturn(kakaoExist);

        // when & then
        assertThrows(BadRequestException.class, () -> signUpService.socialSignUp(request));
    }


    private static Stream<Arguments> validSocial() {
        return Stream.of(
                Arguments.of("google", "kakao", false, false),
                Arguments.of(null, "kakao", false, false),
                Arguments.of("google", null, false, false),
                Arguments.of(null, "kakao", true, false),
                Arguments.of("google", null, false, true)
        );
    }

    @DisplayName("소셜 회원가입 성공")
    @ParameterizedTest
    @MethodSource("validSocial")
    void socialSignUp(String google, String kakao, boolean googleExist, boolean kakaoExist) {
        // given
        OAuthSignUpRequest request = OAuthSignUpRequest.builder()
                .email("test@test.com")
                .name("홍길동")
                .nickname("닉네임")
                .birth(LocalDate.of(2000, 10, 12))
                .googleId(google)
                .kakaoId(kakao)
                .build();

        // mocking
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(memberRepository.existsByNickname(request.getNickname())).thenReturn(false);
        when(memberRepository.existsByGoogleId(request.getGoogleId())).thenReturn(googleExist);
        when(memberRepository.existsByKakaoId(request.getKakaoId())).thenReturn(kakaoExist);

        // when
        signUpService.socialSignUp(request);

        // then
        verify(memberRepository, times(1)).save(any(MemberEntity.class));

    }

}