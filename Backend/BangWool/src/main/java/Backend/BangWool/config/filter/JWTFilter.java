package Backend.BangWool.config.filter;

import Backend.BangWool.member.dto.Session;
import Backend.BangWool.util.CONSTANT;
import Backend.BangWool.util.JWTUtil;
import Backend.BangWool.util.RedisUtil;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 건너 뛰어야 하는 경로 건너뛰기
        if (shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 헤더 찾기
        String authorization = request.getHeader(CONSTANT.AUTHORIZATION_HEADER);

        // Authorization 헤더 검증 (Bearer로 시작하는지 검증)
        if (authorization == null || !authorization.startsWith(CONSTANT.BEARER_PREFIX)) {
            System.out.println("token null");
            setBody(response, 401, "Access token is null");
            return;
        }

        // Bearer 접두사 제거 후 순수 토큰 획득
        String token = authorization.split(" ")[1];

        // 토큰 소멸 시간 검증
        try {
            if (jwtUtil.isExpired(token)) {
                System.out.println("token expired");
                setBody(response, 401, "Access token expired. Please reissue it.");
                return;
            }
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException e) {
            setBody(response, 400,"Token form is incorrect");
            return;
        }

        // 토큰에서 정보 획득
        String category = jwtUtil.getCategory(token);
        int memberID = jwtUtil.getMemberID(token);
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        // access 토큰인지 확인
        if (!category.equals("access")) {
            setBody(response, 400,"It's not a access token");
            return;
        }

        // 파기된 access 토큰인지 확인
        if (redisUtil.getData(CONSTANT.REDIS_TOKEN + token).filter("invalid"::equals).isPresent()) {
            setBody(response, 400, "Invalid token");
            return;
        }


        // 매 요청마다 ContextHolder에 Authentication 추가 (왜냐하면 Stateless이니까)
        Session session = new Session(memberID, username, role);
        Authentication authToken = new UsernamePasswordAuthenticationToken(session, null, session.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken); // user session 생성

        // 다음 필터로 넘기기
        filterChain.doFilter(request, response);
    }

    private void setBody(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        String jsonResponse = "{\"code\": " + code + ", \"message\": " + message + "}";
        response.getWriter().write(jsonResponse);
    }

    private boolean shouldSkip(HttpServletRequest request) {
        List<String> skipURI = new ArrayList<>();
        skipURI.add("/login"); skipURI.add("/login/oauth"); skipURI.add("/swagger-ui/.*"); skipURI.add("/api-docs/.*"); skipURI.add("/auth/.*");

        return skipURI.stream().anyMatch(uri -> {
            Pattern pattern = Pattern.compile(uri);
            return pattern.matcher(request.getRequestURI()).matches();
        });
    }

}
