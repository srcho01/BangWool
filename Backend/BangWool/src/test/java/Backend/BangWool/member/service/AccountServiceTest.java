package Backend.BangWool.member.service;

import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.exception.NotFoundException;
import Backend.BangWool.member.domain.MemberEntity;
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

        // mocking
        when(memberRepository.findByEmailAndNameAndBirth(email, name, birth)).thenReturn(Optional.empty());

        // when & then
        NotFoundException e = assertThrows(NotFoundException.class, () -> accountService.sendEmailForPassword(email, name, birth));
        assertThat(e.getMessage()).isEqualTo("No users have that email, name and birth.");
    }

    @DisplayName("이메일 인증 코드 전송 - 성공")
    @Test
    void sendEmailForPassword() {
        // given
        String email = "test@test.com";
        String name = "test";
        LocalDate birth = LocalDate.of(1990, 1, 1);

        // mocking
        MemberEntity member = MemberEntity.builder().email(email).build();
        when(memberRepository.findByEmailAndNameAndBirth(email, name, birth)).thenReturn(Optional.ofNullable(member));
        doNothing().when(emailService).sendEmail(email);

        // when
        boolean result = accountService.sendEmailForPassword(email, name, birth);

        // then
        verify(memberRepository, times(1)).findByEmailAndNameAndBirth(email, name, birth);
        verify(emailService, times(1)).sendEmail(email);
        assertThat(result).isEqualTo(true);
    }


    private static Stream<Arguments> invalidSetPassword() {
        return Stream.of(
                Arguments.of(false, false, "test1234!!", "test1234!!"),
                Arguments.of(true, false, "test1234!!", "test1234!!"),
                Arguments.of(true, true, "test1234!!", "different"),
                Arguments.of(true, true, "test!!", "test!!"),
                Arguments.of(true, true, "test0!", "test0!")
        );
    }

    @DisplayName("새 비밀번호 변경 - 실패")
    @ParameterizedTest
    @MethodSource("invalidSetPassword")
    void setNewPasswordFail(boolean isMemberExist, boolean isVerify, String pw1, String pw2) {
        // given
        String email = "test@test.com";

        // mocking
        when(memberRepository.findByEmail(email)).thenReturn(isMemberExist ? Optional.of(MemberEntity.builder().build()) : Optional.empty());
        when(redisUtil.getData(email)).thenReturn(isVerify ? Optional.of("true") : Optional.empty());

        // when & then
        if (isMemberExist) {
            assertThrows(BadRequestException.class, () -> accountService.setNewPassword(email, pw1, pw2));
        } else {
            assertThrows(NotFoundException.class, () -> accountService.setNewPassword(email, pw1, pw2));
        }
    }

    @DisplayName("새 비밀번호 변경 - 성공")
    @Test
    void setNewPasswordSuccess() {
        // given
        String email = "test@test.com";
        String pw1 = "test1234!!";
        String pw2 = "test1234!!";

        // mocking
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(MemberEntity.builder().build()));
        when(redisUtil.getData(CONSTANT.REDIS_EMAIL_VERIFY + email)).thenReturn(Optional.of("true"));

        // when
        boolean result = accountService.setNewPassword(email, pw1, pw2);

        // then
        verify(memberRepository, times(1)).findByEmail(email);
        verify(memberRepository, times(1)).save(any(MemberEntity.class));
        assertThat(result).isEqualTo(true);
    }

    private static Stream<Arguments> invalidChangePassword() {
        return Stream.of(
                Arguments.of(false, false, "test1234!!", "test1234!!", "test1234!!"),
                Arguments.of(true, false, "test1234!!", "test1234!!", "test1234!!"),
                Arguments.of(true, true, "test1234@@", "test1234!!", "test1234!!"),
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

        // mocking
        MemberEntity member = MemberEntity.builder().email(email).password(bCryptPasswordEncoder.encode(prevPW)).build();

        when(memberRepository.findByEmail(email)).thenReturn(isMemberExist ? Optional.of(member) : Optional.empty());
        when(redisUtil.getData(CONSTANT.REDIS_EMAIL_VERIFY + email)).thenReturn(isVerify ? Optional.of("true") : Optional.empty());

        // when & then
        if (isMemberExist) {
            assertThrows(BadRequestException.class, () -> accountService.changePassword(email, password, newPW1, newPW2));
        } else {
            assertThrows(NotFoundException.class, () -> accountService.changePassword(email, password, newPW1, newPW2));
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

        // mocking
        MemberEntity member = MemberEntity.builder().email(email).password(bCryptPasswordEncoder.encode(prevPW)).build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(redisUtil.getData(CONSTANT.REDIS_EMAIL_VERIFY + email)).thenReturn(Optional.of("true"));

        // when
        boolean result = accountService.changePassword(email, prevPW, newPW1, newPW2);

        // then
        verify(memberRepository, times(1)).findByEmail(email);
        verify(memberRepository, times(1)).save(any(MemberEntity.class));

        assertThat(result).isEqualTo(true);

    }

}
