package Backend.BangWool.member.controller;

import Backend.BangWool.exception.ServerException;
import Backend.BangWool.member.dto.ChangeMemberInfo;
import Backend.BangWool.util.CurrentSession;
import Backend.BangWool.member.dto.ChangePasswordRequest;
import Backend.BangWool.member.dto.MemberInfoResponse;
import Backend.BangWool.member.dto.Session;
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
    public StatusResponse changePassword(@CurrentSession Session session,
                                         @Valid @RequestBody ChangePasswordRequest request) {

        if (userProfileService.changePassword(session, request)) {
            return StatusResponse.of(200);
        }
        throw new ServerException("Internal Server Error");
    }

    @Operation(summary = "회원정보 조회")
    @GetMapping("info")
    public DataResponse<MemberInfoResponse> getMemberInfo(@CurrentSession Session session) {
        MemberInfoResponse response = userProfileService.getMemberInfo(session);
        return DataResponse.of(response);
    }

    @Operation(summary = "회원정보 수정", description = "변경하지 않을 정보는 그대로 다시 넣어 보냅니다. 삭제하려면 null로 설정합니다.")
    @PostMapping("info")
    public DataResponse<MemberInfoResponse> setMemberInfo(@CurrentSession Session session,
                                                          @Valid @RequestBody ChangeMemberInfo request) {
        MemberInfoResponse response = userProfileService.setMemberInfo(session, request);
        return DataResponse.of(response);
    }

}
