package Backend.BangWool.member.service;

import Backend.BangWool.member.dto.EmailCheckRequest;
import Backend.BangWool.member.dto.EmailSendRequest;
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
    @DisplayName("이메일 전송 - 성공")
    void sendEmail() {
        // given
        String email = "test@test.com";
        EmailSendRequest request = EmailSendRequest.builder().email(email).build();

        // mocking
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailService.sendEmail(request);

        // then - 메시지 검증
        verify(mailSender, times(1)).send(mimeMessage);
        verify(redisUtil, times(1)).setDataExpire(eq(CONSTANT.REDIS_EMAIL_CODE + email), anyString(), eq(600L));
    }


    private static Stream<Arguments> invalidCheckCode() {
        return Stream.of(
                Arguments.of("test@test.com", "TEST12", "WRONG!"),
                Arguments.of(null, "TEST12", null),
                Arguments.of("test@test.com", null, "GOOD!!")
        );
    }

    @ParameterizedTest
    @DisplayName("이메일 코드 확인 - 실패")
    @MethodSource("invalidCheckCode") // given
    void checkCodeFail(String email, String code, String redisCode) {
        // given
        EmailCheckRequest request = EmailCheckRequest.builder()
                .email(email)
                .code(code)
                .build();

        // mocking
        if (redisCode == null) {
            when(redisUtil.getData(CONSTANT.REDIS_EMAIL_CODE + email)).thenReturn(Optional.empty());
        } else {
            when(redisUtil.getData(CONSTANT.REDIS_EMAIL_CODE + email)).thenReturn(Optional.of(redisCode));
        }

        // when
        boolean result = emailService.checkCode(request);

        // then
        verify(redisUtil, never()).setDataExpire(eq(CONSTANT.REDIS_EMAIL_VERIFY + email), eq("true"), eq(1800L));
        assertThat(result).isEqualTo(false);
    }

    @Test
    @DisplayName("이메일 코드 확인 - 성공")
    void checkCodeSuccess() {
        // given
        String email = "test@test.com";
        String code = "W63F5S";
        EmailCheckRequest request = EmailCheckRequest.builder()
                .email(email)
                .code(code)
                .build();

        // mocking
        when(redisUtil.getData(CONSTANT.REDIS_EMAIL_CODE + email)).thenReturn(Optional.of(code));

        // when
        boolean result = emailService.checkCode(request);

        // then
        verify(redisUtil, times(1)).getData(eq(CONSTANT.REDIS_EMAIL_CODE + email));
        verify(redisUtil, times(1)).setDataExpire(eq(CONSTANT.REDIS_EMAIL_VERIFY + email), eq("true"), eq(1800L));
        assertThat(result).isEqualTo(true);
    }
}