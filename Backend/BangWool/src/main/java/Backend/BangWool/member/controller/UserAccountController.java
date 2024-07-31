package Backend.BangWool.member.controller;

import Backend.BangWool.member.dto.*;
import Backend.BangWool.member.service.UserAccountService;
import Backend.BangWool.response.DataResponse;
import Backend.BangWool.response.StatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "User Account", description = "회원 계정 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserAccountController {

    private final UserAccountService userAccountService;

    @Operation(summary = "닉네임 확인", description = "사용 가능하면 true, 이미 존재하면 false")
    @GetMapping("nickname-check")
    public DataResponse<Boolean> nicknameCheck(
            @Parameter(description = "확인할 닉네임")
            @RequestParam String nickname) {
        if (userAccountService.nicknameCheck(nickname))
            return DataResponse.of(true);
        return DataResponse.of(false);
    }

    @Operation(summary = "아이디 찾기")
    @GetMapping("lost/id")
    public DataResponse<String> findEmail(@RequestParam String name,
                                          @Parameter(description = "YYYY-MM-DD 형식") @RequestParam LocalDate birth) {
        return DataResponse.of(userAccountService.findEmail(name, birth));
    }

    @Operation(summary = "비밀번호 찾기 이메일 전송", description = "등록된 유저가 없으면 404")
    @PostMapping("lost/password/email-send")
    public StatusResponse sendEmailForPassword(@Valid @RequestBody EmailSendForPasswordRequest request) {
        if (userAccountService.sendEmailForPassword(request)) {
            return StatusResponse.of(200);
        }
        return StatusResponse.of(500);
    }

    @Operation(summary = "비밀번호 찾기 이메일 인증코드 확인", description = "이메일과 입력받은 인증코드를 받아 전송한 코드와 일치하는지 확인")
    @PostMapping("lost/password/email-check")
    public DataResponse<Boolean> mailCheck(@Valid @RequestBody EmailCheckRequest request) {
        if (userAccountService.checkCodeForPassword(request)) {
            return DataResponse.of(true);
        }
        return DataResponse.of(false);
    }

    @Operation(summary = "새 비밀번호 설정")
    @PostMapping("lost/password/new")
    public StatusResponse setNewPassword(@Valid @RequestBody SetPasswordRequest request) {
        if (userAccountService.setNewPassword(request)) {
            return StatusResponse.of(200);
        }
        return StatusResponse.of(500);
    }
    
}
