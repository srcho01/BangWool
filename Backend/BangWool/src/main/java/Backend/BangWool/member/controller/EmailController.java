package Backend.BangWool.member.controller;

import Backend.BangWool.member.dto.EmailRequestDto;
import Backend.BangWool.member.service.EmailService;
import Backend.BangWool.response.DataResponse;
import Backend.BangWool.response.StatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="Email", description = "Email 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("v1/email/")
public class EmailController {

    private final EmailService emailService;

    @Operation(summary = "인증코드 이메일 전송", description = "이메일을 입력으로 받아 6자리 랜덤 코드를 이메일로 전송")
    @PostMapping("send")
    public StatusResponse mailSend(@Valid @RequestBody EmailRequestDto emailDto) {
        emailService.sendEmail(emailDto.getEmail());
        System.out.println("이메일 인증 이메일 :" + emailDto.getEmail()); // 나중에 로깅으로 변경할 것
        return StatusResponse.build(200);
    }


    @Operation(summary = "인증코드 확인", description = "이메일과 입력받은 인증코드를 받아 전송한 코드와 일치하는지 확인")
    @PostMapping("check")
    public DataResponse<Boolean> mailCheck(@Valid @RequestBody EmailRequestDto emailDto) {
        if (emailService.checkCode(emailDto.getEmail(), emailDto.getCode()))
            return DataResponse.build(true);
        return DataResponse.build(false);
    }
}
