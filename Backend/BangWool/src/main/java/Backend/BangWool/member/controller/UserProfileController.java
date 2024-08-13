package Backend.BangWool.member.controller;

import Backend.BangWool.exception.ServerException;
import Backend.BangWool.member.dto.ChangeMemberInfo;
import Backend.BangWool.member.dto.ChangePasswordRequest;
import Backend.BangWool.member.dto.MemberInfoResponse;
import Backend.BangWool.member.dto.Session;
import Backend.BangWool.member.service.UserProfileService;
import Backend.BangWool.response.DataResponse;
import Backend.BangWool.response.StatusResponse;
import Backend.BangWool.util.CurrentSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PatchMapping("info")
    public DataResponse<MemberInfoResponse> setMemberInfo(@CurrentSession Session session,
                                                          @Valid @RequestBody ChangeMemberInfo request) {
        MemberInfoResponse response = userProfileService.setMemberInfo(session, request);
        return DataResponse.of(response);
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("withdrawal")
    public StatusResponse withdrawal(@CurrentSession Session session) {
        userProfileService.withdrawal(session);
        return StatusResponse.of(200);
    }

    @Operation(summary = "프로필 사진 추가", description = "Response : 설정한 프로필 사진 URL")
    @PutMapping(value = "profile/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DataResponse<String> profileUpload(@CurrentSession Session session,
                                        @RequestPart(value = "profile") MultipartFile image) {
        String uri = userProfileService.profileUpload(session, image);
        return DataResponse.of(uri);
    }

    @Operation(summary = "프로필 사진 삭제", description = "Response : default profile image URL")
    @PatchMapping("profile/delete")
    public DataResponse<String> profileDelete(@CurrentSession Session session) {
        String uri = userProfileService.profileDelete(session);
        return DataResponse.of(uri);
    }

}
