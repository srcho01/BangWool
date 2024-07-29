package Backend.BangWool.member.controller;

import Backend.BangWool.member.dto.*;
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
        return DataResponse.of(accountService.findEmail(name, birth));
    }

    @Operation(summary = "비밀번호 찾기 이메일 전송", description = "등록된 유저가 없으면 404")
    @PostMapping("/auth/lost/password/email-send")
    public StatusResponse sendEmailForPassword(@Valid @RequestBody EmailSendForPasswordRequest request) {
        if (accountService.sendEmailForPassword(request)) {
            return StatusResponse.of(200);
        }
        return StatusResponse.of(500);
    }

    @Operation(summary = "비밀번호 찾기 이메일 인증코드 확인", description = "이메일과 입력받은 인증코드를 받아 전송한 코드와 일치하는지 확인")
    @PostMapping("/auth/lost/password/email-check")
    public DataResponse<Boolean> mailCheck(@Valid @RequestBody EmailCheckRequest request) {
        if (accountService.checkCodeForPassword(request)) {
            return DataResponse.of(true);
        }
        return DataResponse.of(false);
    }

    @Operation(summary = "새 비밀번호 설정")
    @PostMapping("/auth/lost/password/new")
    public StatusResponse setNewPassword(@Valid @RequestBody SetPasswordRequest request) {
        if (accountService.setNewPassword(request)) {
            return StatusResponse.of(200);
        }
        return StatusResponse.of(500);
    }
    
    @Operation(summary = "비밀번호 변경")
    @PostMapping("/user/password/change")
    public StatusResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        if (accountService.changePassword(request)) {
            return StatusResponse.of(200);
        }
        return StatusResponse.of(500);
    }

    @Operation(summary = "회원정보 조회")
    @GetMapping("/user/info")
    public DataResponse<MemberInfoResponse> getMemberInfo(@RequestParam String email) {
        MemberInfoResponse response = accountService.getMemberInfo(email);
        return DataResponse.of(response);
    }

}
