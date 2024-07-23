package Backend.BangWool.member.controller;

import Backend.BangWool.member.dto.LocalSignUpRequest;
import Backend.BangWool.member.dto.OAuthSignUpRequest;
import Backend.BangWool.member.service.SignUpService;
import Backend.BangWool.response.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name="Sign Up", description = "회원가입 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("auth/signup/")
public class SignUpController {

    private final SignUpService signUpService;

    @Operation(summary = "닉네임 확인", description = "사용 가능하면 true, 이미 존재하면 false")
    @GetMapping("nickname-check")
    public DataResponse<Boolean> nicknameCheck(
            @Parameter(description = "확인할 닉네임")
            @RequestParam String nickname) {
        if (signUpService.nicknameCheck(nickname))
            return DataResponse.build(true);
        return DataResponse.build(false);
    }

    @Operation(summary = "자체 회원가입")
    @PostMapping("local")
    public DataResponse<Boolean> localSignUp(@Valid @RequestBody LocalSignUpRequest signUpRequestDto) {
        if (signUpService.localSignUp(signUpRequestDto))
            return DataResponse.build(true);
        return DataResponse.build(false);
    }

    @Operation(summary = "소셜 회원가입", description = "※ googleId와 kakaoId 둘 다 없으면 안됩니다 ※")
    @PostMapping("oauth")
    public DataResponse<Boolean> socialSignUp(@Valid @RequestBody OAuthSignUpRequest signUpRequestDto) {
        if (signUpService.socialSignUp(signUpRequestDto))
            return DataResponse.build(true);
        return DataResponse.build(false);
    }

}
