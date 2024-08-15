package Backend.BangWool.member.service;

import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.member.dto.TokenRefreshRequest;
import Backend.BangWool.member.dto.TokenResponse;
import Backend.BangWool.util.CONSTANT;
import Backend.BangWool.util.JWTUtil;
import Backend.BangWool.util.RedisUtil;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;

    public TokenResponse refresh(TokenRefreshRequest dto) {
        // token 가져오기
        String refresh = dto.refreshToken();

        // expired check
        try {
            if (jwtUtil.isExpired(refresh))
                throw new BadRequestException("Refresh token is expired. Please log in again.");
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new BadRequestException("Token form is incorrect");
        }

        // 정보 가져오기
        String category = jwtUtil.getCategory(refresh);
        Long id = jwtUtil.getId(refresh);
        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);
        long issuedAt = jwtUtil.getIssuedAt(refresh);

        // token이 refresh인지 확인
        if (!category.equals("refresh"))
            throw new BadRequestException("It's not a refresh token");

        // blacklist check
        if (redisUtil.getData(CONSTANT.REDIS_TOKEN + refresh).filter("valid"::equals).isEmpty())
            throw new BadRequestException("Invalid refresh token.");

        // refresh token 블랙리스트 등록
        Date now = new Date();
        long expire = (CONSTANT.REFRESH_EXPIRED - (now.getTime() - issuedAt)/1000) + 10*60; // 남은 시간 + 10분 (단위 : second)
        redisUtil.setDataExpire(CONSTANT.REDIS_TOKEN + refresh, "invalid", expire);

        // token 재발급 (RTR 방식으로 둘 다 재발급)
        String access = jwtUtil.generateToken("access", id, username, role, CONSTANT.ACCESS_EXPIRED);
        refresh = jwtUtil.generateToken("refresh", id, username, role, CONSTANT.REFRESH_EXPIRED);

        // 새 refresh 토큰 redis 저장
        redisUtil.setDataExpire(CONSTANT.REDIS_TOKEN + refresh, "valid", CONSTANT.REFRESH_EXPIRED);

        // 새 토큰 생성
        return new TokenResponse(access, CONSTANT.ACCESS_EXPIRED, refresh, CONSTANT.REFRESH_EXPIRED);
    }

}
