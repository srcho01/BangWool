package Backend.BangWool.config.filter;

import Backend.BangWool.member.dto.CustomUserDetails;
import Backend.BangWool.member.dto.TokenResponse;
import Backend.BangWool.response.DataResponse;
import Backend.BangWool.util.CONSTANT;
import Backend.BangWool.util.JWTUtil;
import Backend.BangWool.util.RedisUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        // 클라이언트 요청에서 username, password 추출
        String username;
        String password;

        Map<String, String> requestBody;
        try {
            requestBody = new ObjectMapper().readValue(request.getInputStream(), new TypeReference<Map<String, String>>(){});
        } catch (IOException e) {
            setBody(response, 400, "Failed to parse JSON request body");
            return null;
        }

        username = requestBody.get("email");
        password = requestBody.get("password");

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            setBody(response, 400, "Email and password are required");
            return null;
        }

        // 스프링 시큐리티에서 username과 pw를 검증하기 위해 token에 담기
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                username, password
        );

        // token에 담은 정보를 검증하기 위해 AuthenticationManager로 전달
        return authenticationManager.authenticate(token);
    }

    // 로그인 성공 시
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {

        // 현재 요청의 유저는 누구인가
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal(); // 사용자의 주체 반환

        // 주체(principal)이란. 일반적으로 사용자 이름이나 사용자 정보를 나타내는 객체
        String username = customUserDetails.getUsername();
        int memberID = customUserDetails.getMemberID();

        // role 알아내기
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        // token 만들기 (expiredSec : 초 단위)
        String access = jwtUtil.generateToken("access", memberID, username, role, CONSTANT.ACCESS_EXPIRED);
        String refresh = jwtUtil.generateToken("refresh", memberID, username, role, CONSTANT.REFRESH_EXPIRED);

        // refresh 토큰 redis 저장
        redisUtil.setDataExpire(CONSTANT.REDIS_TOKEN + refresh, "valid", CONSTANT.REFRESH_EXPIRED);


        // 응답 구성하기 (헤더, 바디)
        TokenResponse tokenDto = new TokenResponse(access, CONSTANT.ACCESS_EXPIRED, refresh, CONSTANT.REFRESH_EXPIRED);
        DataResponse<TokenResponse> tokenBody = DataResponse.build(tokenDto);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(tokenBody);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse);
    }

    // 로그인 실패 시
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        setBody(response, 401, "Unauthorized");
    }

    private void setBody(HttpServletResponse response, int code, String message) {
        try {
            response.setStatus(code);
            response.setContentType("application/json");
            String jsonResponse = "{\"code\": " + code + ", \"message\": " + message + "}";
            response.getWriter().write(jsonResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
