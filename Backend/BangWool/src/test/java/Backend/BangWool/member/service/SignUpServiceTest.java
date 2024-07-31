package Backend.BangWool.member.service;

import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.LocalSignUpRequest;
import Backend.BangWool.member.dto.OAuthSignUpRequest;
import Backend.BangWool.member.repository.MemberRepository;
import Backend.BangWool.util.RedisUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
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
    private AccountService accountService;

    @Autowired
    private SignUpService signUpService;


    @DisplayName("자체 회원가입 - 실패 : 이메일 중복")
    @Test
    void localSignUpFail() {
        // given
        String email = "test@test.com";
        String pw1 = "test1234!!";
        String pw2 = "test1234!!";
        String nickname = "test";

        LocalSignUpRequest request = LocalSignUpRequest.builder()
                .email(email)
                .password1(pw1)
                .password2(pw2)
                .name("홍길동")
                .nickname(nickname)
                .birth(LocalDate.of(2000, 10, 12))
                .build();

        // mocking
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(true);
        when(accountService.nicknameCheck(nickname)).thenReturn(true);
        doNothing().when(accountService).passwordCheck(pw1, pw2);
        when(accountService.nicknameCheck(nickname)).thenReturn(true);

        // when & then
        assertThrows(BadRequestException.class, () -> signUpService.localSignUp(request));

    }

    @DisplayName("자체 회원가입 - 성공")
    @Test
    void localSignUp() {
        // given
        String email = "test@test.com";
        String pw1 = "test1234!!";
        String pw2 = "test1234!!";
        String nickname = "test";

        LocalSignUpRequest request = LocalSignUpRequest.builder()
                .email(email)
                .password1(pw1)
                .password2(pw2)
                .name("홍길동")
                .nickname(nickname)
                .birth(LocalDate.of(2000, 10, 12))
                .build();

        // mocking
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(accountService.nicknameCheck(nickname)).thenReturn(true);
        doNothing().when(accountService).passwordCheck(pw1, pw2);
        when(accountService.nicknameCheck(nickname)).thenReturn(true);

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

    @DisplayName("소셜 회원가입 - 실패")
    @ParameterizedTest
    @MethodSource("invalidSocial")
    void socialSignUpFail(String google, String kakao, boolean googleExist, boolean kakaoExist) {
        // given
        String nickname = "test";

        OAuthSignUpRequest request = OAuthSignUpRequest.builder()
                .email("test@test.com")
                .name("홍길동")
                .nickname(nickname)
                .birth(LocalDate.of(2000, 10, 12))
                .googleId(google)
                .kakaoId(kakao)
                .build();

        // mocking
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(memberRepository.existsByGoogleId(request.getGoogleId())).thenReturn(googleExist);
        when(memberRepository.existsByKakaoId(request.getKakaoId())).thenReturn(kakaoExist);
        when(accountService.nicknameCheck(nickname)).thenReturn(true);

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

    @DisplayName("소셜 회원가입 - 성공")
    @ParameterizedTest
    @MethodSource("validSocial")
    void socialSignUp(String google, String kakao, boolean googleExist, boolean kakaoExist) {
        // given
        String nickname = "test";

        OAuthSignUpRequest request = OAuthSignUpRequest.builder()
                .email("test@test.com")
                .name("홍길동")
                .nickname(nickname)
                .birth(LocalDate.of(2000, 10, 12))
                .googleId(google)
                .kakaoId(kakao)
                .build();

        // mocking
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(memberRepository.existsByGoogleId(request.getGoogleId())).thenReturn(googleExist);
        when(memberRepository.existsByKakaoId(request.getKakaoId())).thenReturn(kakaoExist);
        when(accountService.nicknameCheck(nickname)).thenReturn(true);

        // when
        signUpService.socialSignUp(request);

        // then
        verify(memberRepository, times(1)).save(any(MemberEntity.class));

    }

}