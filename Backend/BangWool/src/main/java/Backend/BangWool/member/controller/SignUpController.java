package Backend.BangWool.member.controller;

import Backend.BangWool.member.dto.LocalSignUpRequest;
import Backend.BangWool.member.dto.MemberInfoResponse;
import Backend.BangWool.member.dto.OAuthSignUpRequest;
import Backend.BangWool.member.service.SignUpService;
import Backend.BangWool.response.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="Sign Up", description = "회원가입 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("auth/signup/")
public class SignUpController {

    private final SignUpService signUpService;


    @Operation(summary = "자체 회원가입")
    @PostMapping("local")
    public DataResponse<MemberInfoResponse> localSignUp(@Valid @RequestBody LocalSignUpRequest request) {
        MemberInfoResponse response = signUpService.localSignUp(request);
        return DataResponse.of(response);
    }

    @Operation(summary = "소셜 회원가입", description = "※ googleId와 kakaoId 둘 다 없으면 안됩니다 ※")
    @PostMapping("oauth")
    public DataResponse<MemberInfoResponse> socialSignUp(@Valid @RequestBody OAuthSignUpRequest request) {
        MemberInfoResponse response = signUpService.socialSignUp(request);
        return DataResponse.of(response);
    }

}
