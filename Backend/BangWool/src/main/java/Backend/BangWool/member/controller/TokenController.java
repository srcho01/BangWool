package Backend.BangWool.member.controller;

import Backend.BangWool.member.dto.TokenRefreshRequestDto;
import Backend.BangWool.member.dto.TokenResponseDto;
import Backend.BangWool.response.DataResponse;
import Backend.BangWool.response.ResponseDTO;
import Backend.BangWool.response.StatusResponse;
import Backend.BangWool.util.CONSTANT;
import Backend.BangWool.util.JWTUtil;
import Backend.BangWool.util.RedisUtil;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Tag(name = "Token", description = "Token 재발급")
@RestController
@RequiredArgsConstructor
@RequestMapping("auth/")
public class TokenController {

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Operation(summary = "refresh token을 사용한 access token 재발급")
    @ApiResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DataResponse.class),
                    examples = @ExampleObject(
                            value = "{\"code\": \"200\", \"message\": \"OK\", \"data\": {\"accessToken\": \"new_access_token\", \"refreshToken\": \"new_refresh_token\"}}"
                    )
            )
    )
    @PostMapping("refresh")
    public ResponseDTO refresh(@Valid @RequestBody TokenRefreshRequestDto tokenDto) {

        // token 가져오기
        String refresh = tokenDto.refreshToken();

        // expired check
        try {
            if (jwtUtil.isExpired(refresh))
                return StatusResponse.build(400, "Refresh token is expired. Please log in again.");
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException e) {
            return StatusResponse.build(400, "Token form is incorrect");
        }

        // 정보 가져오기
        String category = jwtUtil.getCategory(refresh);
        int memberID = jwtUtil.getMemberID(refresh);
        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);
        long issuedAt = jwtUtil.getIssuedAt(refresh);

        // token이 refresh인지 확인
        if (!category.equals("refresh"))
            return StatusResponse.build(400, "It's not a refresh token");

        // blacklist check
        if (redisUtil.exists(CONSTANT.REDIS_TOKEN + refresh) && !redisUtil.getData(CONSTANT.REDIS_TOKEN + refresh).equals("valid"))
            return StatusResponse.build(400, "Invalid refresh token.");

        // refresh token 블랙리스트 등록
        Date now = new Date();
        long expire = (CONSTANT.REFRESH_EXPIRED - (now.getTime() - issuedAt)/1000) + 10*60; // 남은 시간 + 10분 (단위 : second)
        redisUtil.setDataExpire(CONSTANT.REDIS_TOKEN + refresh, "invalid", expire);

        // token 재발급 (RTR 방식으로 둘 다 재발급)
        String access = jwtUtil.generateToken("access", memberID, username, role, CONSTANT.ACCESS_EXPIRED);
        refresh = jwtUtil.generateToken("refresh", memberID, username, role, CONSTANT.REFRESH_EXPIRED);

        // 새 refresh 토큰 redis 저장
        redisUtil.setDataExpire(CONSTANT.REDIS_TOKEN + refresh, "valid", CONSTANT.REFRESH_EXPIRED);

        // 새 토큰 응답
        TokenResponseDto newToken = new TokenResponseDto(access, CONSTANT.ACCESS_EXPIRED, refresh, CONSTANT.REFRESH_EXPIRED);
        return DataResponse.build(newToken);
    }

}