package Backend.BangWool.member.controller;

import Backend.BangWool.member.dto.LocalSignUpRequestDto;
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
@RequestMapping("v1/signup/")
public class SignUpController {

    private final SignUpService signUpService;

    @Operation(summary = "자체 회원가입")
    @PostMapping("local")
    public DataResponse<Boolean> localSignUp(@Valid @RequestBody LocalSignUpRequestDto signUpRequestDto) {
        if (signUpService.localSignUp(signUpRequestDto))
            return DataResponse.build(true);
        return DataResponse.build(false);
    }

}
