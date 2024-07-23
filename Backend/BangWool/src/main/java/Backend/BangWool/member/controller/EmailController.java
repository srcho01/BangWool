package Backend.BangWool.member.controller;

import Backend.BangWool.member.dto.EmailCheckRequest;
import Backend.BangWool.member.dto.EmailSendRequest;
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
@RequestMapping("auth/email/")
public class EmailController {

    private final EmailService emailService;

    @Operation(summary = "인증코드 이메일 전송", description = "이메일을 입력으로 받아 6자리 랜덤 코드를 이메일로 전송")
    @PostMapping("send")
    public StatusResponse mailSend(@Valid @RequestBody EmailSendRequest sendDto) {
        emailService.sendEmail(sendDto.getEmail());
        return StatusResponse.build(200);
    }


    @Operation(summary = "인증코드 확인", description = "이메일과 입력받은 인증코드를 받아 전송한 코드와 일치하는지 확인")
    @PostMapping("check")
    public DataResponse<Boolean> mailCheck(@Valid @RequestBody EmailCheckRequest checkDto) {
        if (emailService.checkCode(checkDto.getEmail(), checkDto.getCode()))
            return DataResponse.build(true);
        return DataResponse.build(false);
    }
}
