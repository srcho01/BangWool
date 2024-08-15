package Backend.BangWool.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {

    private final SecretKey secretKey;
    private final RedisUtil redisUtil;

    // secret key 가져오기
    public JWTUtil(@Value("${spring.jwt.secret}")String secret, RedisUtil redisUtil) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.redisUtil = redisUtil;
    }

    // 토큰 발급
    public String generateToken(String category, Long id, String username, String role, Long expiredSec) {
        long expiredMs = expiredSec * 1000;
        return Jwts.builder()
                .claim("category", category)
                .claim("id", id)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    // 토큰 파기
    public void expireToken(String type, String token) {
        long issuedAt = getIssuedAt(token);

        Date now = new Date();
        if (type.equals("access")) {
            long expire = (CONSTANT.ACCESS_EXPIRED - (now.getTime() - issuedAt)/1000) + 10*60; // 남은 시간 + 10분
            redisUtil.setDataExpire(CONSTANT.REDIS_TOKEN + token, "invalid", expire);
        } else if (type.equals("refresh")) {
            long expire = (CONSTANT.REFRESH_EXPIRED - (now.getTime() - issuedAt)/1000) + 10*60; // 남은 시간 + 10분
            redisUtil.setDataExpire(CONSTANT.REDIS_TOKEN + token, "invalid", expire);
        }
    }

    // id 가져오기
    public Long getId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("id", Long.class);
    }

    // 토큰 유저 이름 추출
    public String getUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    // 롤 가져오기
    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    // 발행시간 가져오기
    public long getIssuedAt(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getIssuedAt().getTime();
    }

    // category 가져오기
    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    // 토큰 만료 확인
    public boolean isExpired(String token) throws MalformedJwtException {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

}
