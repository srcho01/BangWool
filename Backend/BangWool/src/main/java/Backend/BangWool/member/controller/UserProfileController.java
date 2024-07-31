package Backend.BangWool.member.controller;

import Backend.BangWool.member.dto.ChangePasswordRequest;
import Backend.BangWool.member.dto.MemberInfoResponse;
import Backend.BangWool.member.service.UserProfileService;
import Backend.BangWool.response.DataResponse;
import Backend.BangWool.response.StatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;


    @Operation(summary = "비밀번호 변경")
    @PostMapping("/user/password/change")
    public StatusResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        if (userProfileService.changePassword(request)) {
            return StatusResponse.of(200);
        }
        return StatusResponse.of(500);
    }

    @Operation(summary = "회원정보 조회")
    @GetMapping("/user/info")
    public DataResponse<MemberInfoResponse> getMemberInfo(@RequestParam String email) {
        MemberInfoResponse response = userProfileService.getMemberInfo(email);
        return DataResponse.of(response);
    }

}
