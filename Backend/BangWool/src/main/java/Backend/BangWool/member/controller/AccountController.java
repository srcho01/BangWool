package Backend.BangWool.member.controller;

import Backend.BangWool.member.dto.EmailCheckRequest;
import Backend.BangWool.member.dto.EmailSendForPasswordRequest;
import Backend.BangWool.member.service.AccountService;
import Backend.BangWool.response.DataResponse;
import Backend.BangWool.response.StatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Account", description = "회원 계정 관련 API")
@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "아이디 찾기")
    @GetMapping("/auth/lost/id")
    public DataResponse<String> findEmail(@RequestParam String name,
                                          @Parameter(description = "YYYY-MM-DD 형식") @RequestParam LocalDate birth) {
        return DataResponse.build(accountService.findEmail(name, birth));
    }

    @Operation(summary = "비밀번호 찾기 이메일 전송", description = "등록된 유저가 없으면 404")
    @PostMapping("/auth/lost/password/email-send")
    public StatusResponse sendEmailForPassword(@Valid @RequestBody EmailSendForPasswordRequest dto) {
        if (accountService.sendEmailForPassword(dto.getEmail(), dto.getName(), dto.getBirth())) {
            return StatusResponse.build(200);
        }
        return StatusResponse.build(500);
    }

    @Operation(summary = "비밀번호 찾기 이메일 인증코드 확인", description = "이메일과 입력받은 인증코드를 받아 전송한 코드와 일치하는지 확인")
    @PostMapping("/auth/lost/password/email-check")
    public DataResponse<Boolean> mailCheck(@Valid @RequestBody EmailCheckRequest dto) {
        if (accountService.checkCodeForPassword(dto.getEmail(), dto.getCode()))
            return DataResponse.build(true);
        return DataResponse.build(false);
    }

}