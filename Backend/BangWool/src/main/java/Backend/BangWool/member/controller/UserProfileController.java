package Backend.BangWool.member.controller;

import Backend.BangWool.member.dto.ChangePasswordRequest;
import Backend.BangWool.member.dto.MemberInfoResponse;
import Backend.BangWool.member.service.UserProfileService;
import Backend.BangWool.response.DataResponse;
import Backend.BangWool.response.StatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Profile", description = "회원 정보 수정 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserProfileController {

    private final UserProfileService userProfileService;


    @Operation(summary = "비밀번호 변경")
    @PostMapping("password/change")
    public StatusResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        if (userProfileService.changePassword(request)) {
            return StatusResponse.of(200);
        }
        return StatusResponse.of(500);
    }

    @Operation(summary = "회원정보 조회")
    @GetMapping("info")
    public DataResponse<MemberInfoResponse> getMemberInfo(@RequestParam String email) {
        MemberInfoResponse response = userProfileService.getMemberInfo(email);
        return DataResponse.of(response);
    }

}
