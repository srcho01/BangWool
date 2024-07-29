package Backend.BangWool.member.service;

import Backend.BangWool.exception.ServerException;
import Backend.BangWool.member.dto.EmailCheckRequest;
import Backend.BangWool.member.dto.EmailSendRequest;
import Backend.BangWool.util.CONSTANT;
import Backend.BangWool.util.RedisUtil;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final RedisUtil redis;

    private String createCode() {
        Random r = new Random();
        final String CHAR = "ABCDEFGHIJKLMNOPQRSTUVWXYZ01223456789";

        StringBuilder code = new StringBuilder();
        for (int i=0; i<6; i++) {
            code.append(CHAR.charAt(r.nextInt(CHAR.length())));
        }

        return code.toString();
    }

    @Async
    @Transactional
    public void sendEmail(EmailSendRequest request) {

        String email = request.getEmail();

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        String code = createCode();

        String content =
                "<html>" +
                    "<body>" +
                    "<p> 본인이 맞으시면 아래 인증 코드를 앱에 입력해주세요.</p>" +
                    "<br><br>" +
                    "<h3>" + code + "</h3>" +
                    "<br><br>" +
                    "<p> 감사합니다. </p>" +
                    "</body>" +
                "</html>";

        redis.setDataExpire(CONSTANT.REDIS_EMAIL_CODE + email, code, 10 * 60L); // 10minutes

        try {
            helper.setTo(email);
            helper.setSubject("방울 이메일 인증 코드입니다");
            helper.setText(content, true);

            mailSender.send(message);

        } catch (Exception e) {
            redis.deleteData(CONSTANT.REDIS_EMAIL_CODE + email);
            throw new ServerException("Failed to send authentication email");
        }
    }

    public boolean checkCode(EmailCheckRequest request) {

        String email = request.getEmail();
        String code = request.getCode();

        if (code != null && redis.getData(CONSTANT.REDIS_EMAIL_CODE + email).filter(code::equals).isPresent()) {
            redis.setDataExpire(CONSTANT.REDIS_EMAIL_VERIFY + email, "true", 30 * 60L); // 30minutes
            return true;
        }
        return false;
    }

}