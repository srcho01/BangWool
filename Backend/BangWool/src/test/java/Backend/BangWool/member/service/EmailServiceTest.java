package Backend.BangWool.member.service;

import Backend.BangWool.util.CONSTANT;
import Backend.BangWool.util.RedisUtil;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class EmailServiceTest {

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private RedisUtil redisUtil;

    @Autowired
    private EmailService emailService;

    @Test
    @DisplayName("이메일 보내기")
    void sendEmail() throws Exception {
        // given
        String email = "test@test.com";

        // mocking
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailService.sendEmail(email);

        // then - 메시지 검증
        verify(mailSender, times(1)).send(mimeMessage);
        verify(redisUtil, times(1)).setDataExpire(eq(CONSTANT.REDIS_EMAIL_CODE + email), anyString(), eq(600L));
    }

    @Test
    @DisplayName("이메일 코드 확인 성공")
    void checkCodeSuccess() {
        // given
        String email = "test@test.com";
        String code = "W63F5S";

        // mocking
        when(redisUtil.getData(CONSTANT.REDIS_EMAIL_CODE + email)).thenReturn(Optional.of(code));

        // when
        boolean result = emailService.checkCode(email, code);

        // then
        verify(redisUtil, times(1)).getData(eq(CONSTANT.REDIS_EMAIL_CODE + email));
        verify(redisUtil, times(1)).setDataExpire(eq(CONSTANT.REDIS_EMAIL_VERIFY + email), eq("true"), eq(1800L));
        assertThat(result).isEqualTo(true);
    }

    private static Stream<Arguments> invalidCheckCode() {
        return Stream.of(
                Arguments.of("test@test.com", "TEST12", "WRONG!"),
                Arguments.of(null, "TEST12", null),
                Arguments.of("test@test.com", null, "GOOD!!")
        );
    }

    @ParameterizedTest
    @DisplayName("이메일 코드 확인 실패")
    @MethodSource("invalidCheckCode") // given
    void checkCodeFail(String email, String code, String redisCode) {
        // mocking
        if (redisCode == null) {
            when(redisUtil.getData(CONSTANT.REDIS_EMAIL_CODE + email)).thenReturn(Optional.empty());
        } else {
            when(redisUtil.getData(CONSTANT.REDIS_EMAIL_CODE + email)).thenReturn(Optional.of(redisCode));
        }

        // when
        boolean result = emailService.checkCode(email, code);

        // then
        verify(redisUtil, never()).setDataExpire(eq(CONSTANT.REDIS_EMAIL_VERIFY + email), eq("true"), eq(1800L));
        assertThat(result).isEqualTo(false);
    }

}