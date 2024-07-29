package Backend.BangWool.member.controller;

import Backend.BangWool.member.dto.TokenRefreshRequest;
import Backend.BangWool.member.dto.TokenResponse;
import Backend.BangWool.member.service.TokenService;
import Backend.BangWool.response.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Token", description = "Token 재발급")
@RestController
@RequiredArgsConstructor
@RequestMapping("auth/")
public class TokenController {

    private final TokenService tokenService;

    @Operation(summary = "refresh token을 사용한 access token 재발급")
    @PostMapping("refresh")
    public DataResponse<TokenResponse> refresh(@Valid @RequestBody TokenRefreshRequest dto) {
        return DataResponse.of(tokenService.refresh(dto));
    }

}