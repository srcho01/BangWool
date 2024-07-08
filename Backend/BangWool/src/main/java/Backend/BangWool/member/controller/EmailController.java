package Backend.BangWool.member.controller;

import Backend.BangWool.member.dto.EmailDTO;
import Backend.BangWool.member.service.EmailService;
import Backend.BangWool.util.CustomResponse;
import Backend.BangWool.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("email/")
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<CustomResponse> mailSend(@Valid @RequestBody EmailDTO emailDto) {
        emailService.sendEmail(emailDto.getEmail());
        System.out.println("이메일 인증 이메일 :" + emailDto.getEmail()); // 나중에 로깅으로 변경할 것
        return ResponseUtil.build(HttpStatus.OK);
    }

    @PostMapping("/check")
    public ResponseEntity<CustomResponse> mailCheck(@Valid @RequestBody EmailDTO emailDto) {
        if (emailService.checkCode(emailDto.getEmail(), emailDto.getCode()))
            return ResponseUtil.build(HttpStatus.OK, "Email authentication was successful");
        return ResponseUtil.build(HttpStatus.BAD_REQUEST, "Email authentication was fail");
    }
}
