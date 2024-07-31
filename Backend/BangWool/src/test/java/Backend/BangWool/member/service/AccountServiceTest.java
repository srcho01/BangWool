package Backend.BangWool.member.service;

import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.exception.NotFoundException;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.*;
import Backend.BangWool.member.repository.MemberRepository;
import Backend.BangWool.util.CONSTANT;
import Backend.BangWool.util.RedisUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class AccountServiceTest {

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private EmailService emailService;

    @MockBean
    private RedisUtil redisUtil;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    AccountService accountService;


    @DisplayName("닉네임 확인 - 실패 : 닉네임 조건 미충족")
    @ParameterizedTest
    @ValueSource(strings = {"!@#$", "닉네임이너무길어123456"})
    void nicknameCheckFail(String nickname) {
        // when & then
        BadRequestException e = assertThrows(BadRequestException.class, () -> accountService.nicknameCheck(nickname));
        assertThat(e.getMessage()).isEqualTo("The nickname must be 1 to 10 characters, consisting only of English, numbers, and Korean.");
    }

    @DisplayName("닉네임 확인 - 실패 : 이미 존재하는 닉네임")
    @Test
    void nicknameCheckFail2() {
        // given
        String nickname = "test";

        // mocking
        when(memberRepository.existsByNickname(nickname)).thenReturn(true);

        // when & then
        BadRequestException e = assertThrows(BadRequestException.class, () -> accountService.nicknameCheck(nickname));
        assertThat(e.getMessage()).isEqualTo("Nickname is already existed.");
    }


    @DisplayName("이메일 찾기 - 실패 : 없는 유저")
    @Test
    void findEmailFail() {
        // given
        String name = "test";
        LocalDate birth = LocalDate.of(1990, 1, 1);

        // mocking
        when(memberRepository.findByNameAndBirth(name, birth)).thenReturn(Optional.empty());

        // when & then
        NotFoundException e = assertThrows(NotFoundException.class, () -> accountService.findEmail(name, birth));
        assertThat(e.getMessage()).isEqualTo("No users have that name and birth.");
    }

    @DisplayName("이메일 찾기 - 성공")
    @Test
    void findEmailSuccess() {
        // given
        String name = "test";
        LocalDate birth = LocalDate.of(1990, 1, 1);
        String email = "test@test.com";

        // mocking
        MemberEntity member = MemberEntity.builder().email(email).build();
        when(memberRepository.findByNameAndBirth(name, birth)).thenReturn(Optional.ofNullable(member));

        // when
        String result = accountService.findEmail(name, birth);

        // then
        assertThat(result).isEqualTo(email);
    }


    @DisplayName("이메일 인증 코드 전송 - 실패 : 존재하지 않는 유저")
    @Test
    void sendEmailForPasswordFail() {
        // given
        String email = "test@test.com";
        String name = "test";
        LocalDate birth = LocalDate.of(1990, 1, 1);
        EmailSendForPasswordRequest request = EmailSendForPasswordRequest.builder()
                .email(email)
                .name(name)
                .birth(birth)
                .build();

        // mocking
        when(memberRepository.findByEmailAndNameAndBirth(email, name, birth)).thenReturn(Optional.empty());

        // when & then
        NotFoundException e = assertThrows(NotFoundException.class, () -> accountService.sendEmailForPassword(request));
        assertThat(e.getMessage()).isEqualTo("No users have that email, name and birth.");
    }

    @DisplayName("이메일 인증 코드 전송 - 성공")
    @Test
    void sendEmailForPassword() {
        // given
        String email = "test@test.com";
        String name = "test";
        LocalDate birth = LocalDate.of(1990, 1, 1);
        EmailSendForPasswordRequest request = EmailSendForPasswordRequest.builder()
                .email(email)
                .name(name)
                .birth(birth)
                .build();

        // mocking
        MemberEntity member = MemberEntity.builder().email(email).build();
        when(memberRepository.findByEmailAndNameAndBirth(email, name, birth)).thenReturn(Optional.ofNullable(member));
        doNothing().when(emailService).sendEmail(EmailSendRequest.builder().email(email).build());

        // when
        boolean result = accountService.sendEmailForPassword(request);

        // then
        verify(memberRepository, times(1)).findByEmailAndNameAndBirth(email, name, birth);
        verify(emailService, times(1)).sendEmail(any(EmailSendRequest.class));
        assertThat(result).isEqualTo(true);
    }


    private static Stream<Arguments> invalidSetPassword() {
        return Stream.of(
                Arguments.of(false, false, "test1234!!", "test1234!!"), // 유저 없음
                Arguments.of(true, false, "test1234!!", "test1234!!"), // email 확인 미완료
                Arguments.of(true, true, "test1234!!", "different"), // 비밀번호 다름
                Arguments.of(true, true, "test!!", "test!!"), // 비밀번호 조건 부합 X
                Arguments.of(true, true, "test0!", "test0!") // 비밀번호 짧음
        );
    }

    @DisplayName("새 비밀번호 변경 - 실패")
    @ParameterizedTest
    @MethodSource("invalidSetPassword")
    void setNewPasswordFail(boolean isMemberExist, boolean isVerify, String pw1, String pw2) {
        // given
        String email = "test@test.com";
        SetPasswordRequest request = SetPasswordRequest.builder()
                .email(email)
                .password1(pw1)
                .password2(pw2)
                .build();

        // mocking
        when(memberRepository.findByEmail(email)).thenReturn(isMemberExist ? Optional.of(MemberEntity.builder().build()) : Optional.empty());
        when(redisUtil.getData(email)).thenReturn(isVerify ? Optional.of("true") : Optional.empty());

        // when & then
        if (isMemberExist) {
            assertThrows(BadRequestException.class, () -> accountService.setNewPassword(request));
        } else {
            assertThrows(NotFoundException.class, () -> accountService.setNewPassword(request));
        }
    }

    @DisplayName("새 비밀번호 변경 - 성공")
    @Test
    void setNewPasswordSuccess() {
        // given
        String email = "test@test.com";
        String pw1 = "test1234!!";
        String pw2 = "test1234!!";
        SetPasswordRequest request = SetPasswordRequest.builder()
                .email(email)
                .password1(pw1)
                .password2(pw2)
                .build();

        // mocking
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(MemberEntity.builder().build()));
        when(redisUtil.getData(CONSTANT.REDIS_EMAIL_VERIFY + email)).thenReturn(Optional.of("true"));

        // when
        boolean result = accountService.setNewPassword(request);

        // then
        verify(memberRepository, times(1)).findByEmail(email);
        verify(memberRepository, times(1)).save(any(MemberEntity.class));
        assertThat(result).isEqualTo(true);
    }


    private static Stream<Arguments> invalidChangePassword() {
        return Stream.of(
                Arguments.of(false, false, "test1234!!", "test1234!!", "test1234!!"),
                Arguments.of(true, false, "test1234!!", "test1234!!", "test1234!!"),
                Arguments.of(true, true, "test1234@@", "test1234!!", "test1234!!"), // 이전 비밀번호 불일치
                Arguments.of(true, true, "test1234!!", "test1234!!", "different"),
                Arguments.of(true, true, "test1234!!", "test!!", "test!!"),
                Arguments.of(true, true, "test1234!!", "test0!", "test0!")
        );
    }

    @DisplayName("비밀번호 변경 - 실패")
    @ParameterizedTest
    @MethodSource("invalidChangePassword")
    void changePasswordFail(boolean isMemberExist, boolean isVerify, String prevPW, String newPW1, String newPW2) {
        // given
        String password = "test1234!!";
        String email = "test@test.com";
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .email(email)
                .prevPassword(password)
                .password1(newPW1)
                .password2(newPW2)
                .build();

        // mocking
        MemberEntity member = MemberEntity.builder().email(email).password(bCryptPasswordEncoder.encode(prevPW)).build();

        when(memberRepository.findByEmail(email)).thenReturn(isMemberExist ? Optional.of(member) : Optional.empty());
        when(redisUtil.getData(CONSTANT.REDIS_EMAIL_VERIFY + email)).thenReturn(isVerify ? Optional.of("true") : Optional.empty());

        // when & then
        if (isMemberExist) {
            assertThrows(BadRequestException.class, () -> accountService.changePassword(request));
        } else {
            assertThrows(NotFoundException.class, () -> accountService.changePassword(request));
        }
    }

    @DisplayName("비밀번호 변경 - 성공")
    @Test
    void changePasswordSuccess() {
        // given
        String email = "test@test.com";
        String prevPW = "test1234!!";
        String newPW1 = "test123456!!";
        String newPW2 = "test123456!!";
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .email(email)
                .prevPassword(prevPW)
                .password1(newPW1)
                .password2(newPW2)
                .build();

        // mocking
        MemberEntity member = MemberEntity.builder().email(email).password(bCryptPasswordEncoder.encode(prevPW)).build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(redisUtil.getData(CONSTANT.REDIS_EMAIL_VERIFY + email)).thenReturn(Optional.of("true"));

        // when
        boolean result = accountService.changePassword(request);

        // then
        verify(memberRepository, times(1)).findByEmail(email);
        verify(memberRepository, times(1)).save(any(MemberEntity.class));

        assertThat(result).isEqualTo(true);

    }


    @DisplayName("회원정보 조회 - 실패 : 없는 유저")
    @Test
    void getMemberInfoFail() {
        // given
        String email = "test@test.com";

        // mocking
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        NotFoundException e = assertThrows(NotFoundException.class, () -> accountService.getMemberInfo(email));
        assertThat(e.getMessage()).isEqualTo("User not found");
    }

    @DisplayName("회원정보 조회 - 성공")
    @Test
    void getMemberInfoSuccess() {
        // given
        String email = "test@test.com";

        // mocking
        MemberEntity member = MemberEntity.builder()
                .email(email)
                .password("1234")
                .name("test")
                .nickname("test")
                .birth(LocalDate.of(2000, 1, 1))
                .googleId("anfiuownen")
                .kakaoId("12198964732")
                .build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        // when
        MemberInfoResponse result = accountService.getMemberInfo(email);

        // then
        assertThat(result).isInstanceOf(MemberInfoResponse.class);
    }

}
