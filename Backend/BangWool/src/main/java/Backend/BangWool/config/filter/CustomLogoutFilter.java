package Backend.BangWool.config.filter;

import Backend.BangWool.util.CONSTANT;
import Backend.BangWool.util.JWTUtil;
import Backend.BangWool.util.RedisUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Map;

public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;

    public CustomLogoutFilter(JWTUtil jwtUtil, RedisUtil redisUtil) {
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException  {
        doFilter((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        // path check
        String uri = request.getRequestURI();
        if (!uri.matches("^/logout$")) {
            filterChain.doFilter(request, response);
            return;
        }

        // method check
        String method = request.getMethod();
        if (!method.equals("DELETE")) {
            setBody(response, 400, "logout should be DELETE method");
            return;
        }

        // get tokens
        Map<String, String> requestBody;
        try {
            requestBody = new ObjectMapper().readValue(request.getInputStream(), new TypeReference<Map<String, String>>(){});
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON request body", e);
        }

        String access = requestBody.get("accessToken");
        String refresh = requestBody.get("refreshToken");
        if (access == null || refresh == null) {
            setBody(response, 400, "Both access and refresh tokens are required");
            return;
        }


        /* ACCESS TOKEN */
        try {
            if (!jwtUtil.isExpired(access)) { // access 토큰 시간이 남았다면 blacklist 등록
                String category = jwtUtil.getCategory(access);

                // access 토큰인지 확인
                if (!category.equals("access")) {
                    setBody(response, 400,"It's not a access token");
                    return;
                }

                // Access Token 블랙리스트 등록
                jwtUtil.expireToken("access", access);

            }
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException e) {
            setBody(response, 400,"Token form is incorrect");
            return;
        }

        
        /* REFRESH TOKEN */
        // refresh 토큰 유효한지 검사 (유효해야 함)
        try {
            if (jwtUtil.isExpired(refresh)) {
                setBody(response, 401, "Refresh token expired");
                return;
            }
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException | IllegalArgumentException e) {
            setBody(response, 400,"Token form is incorrect");
            return;
        }

        // 토큰에서 정보 획득
        String category = jwtUtil.getCategory(refresh);

        // refresh 토큰인지 확인
        if (!category.equals("refresh")) {
            setBody(response, 400,"It's not a access token");
            return;
        }

        // DB 저장 확인
        if (redisUtil.getData(CONSTANT.REDIS_TOKEN + refresh).filter("valid"::equals).isEmpty()) {
            setBody(response, 400, "Refresh token expired");
            return;
        }

        // Access Token 블랙리스트 등록
        jwtUtil.expireToken("refresh", refresh);

        setBody(response, 200, "OK");
    }

    private void setBody(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json");
        String jsonResponse = "{\"code\": " + code + ", \"message\": " + message + "}";
        response.getWriter().write(jsonResponse);
    }
}
