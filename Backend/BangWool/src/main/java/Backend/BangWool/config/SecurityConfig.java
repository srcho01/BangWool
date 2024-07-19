package Backend.BangWool.config;

import Backend.BangWool.config.auth.SocialAuthenticationManager;
import Backend.BangWool.config.filter.CustomLoginFilter;
import Backend.BangWool.config.filter.CustomLogoutFilter;
import Backend.BangWool.config.filter.JWTFilter;
import Backend.BangWool.config.filter.SocialLoginFilter;
import Backend.BangWool.member.service.CustomUserDetailsService;
import Backend.BangWool.util.JWTUtil;
import Backend.BangWool.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final CustomUserDetailsService customUserDetailsService;


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        // Bean에 AuthenticationManager 등록
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable); // form 로그인 방식
        http.httpBasic(AbstractHttpConfigurer::disable); // http basic 인증 방식


        // 경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api-docs/**", "/swagger-ui/**").permitAll() // Swagger
                        .requestMatchers("/auth/**", "/login/oauth").permitAll()
                        .anyRequest().authenticated()
                );

        // filter 등록
        http
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, redisUtil), LogoutFilter.class)
                .addFilterBefore(new JWTFilter(jwtUtil, redisUtil), SocialLoginFilter.class)
                .addFilterBefore(socialLoginFilter(), CustomLoginFilter.class)
                .addFilterAt(new CustomLoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, redisUtil), UsernamePasswordAuthenticationFilter.class);

        //세션 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public AuthenticationManager socialAuthenticationManager() {
        return new SocialAuthenticationManager(customUserDetailsService);
    }

    @Bean
    public SocialLoginFilter socialLoginFilter() throws Exception {
        SocialLoginFilter filter = new SocialLoginFilter(socialAuthenticationManager(), jwtUtil, redisUtil);
        filter.setFilterProcessesUrl("/login/oauth");
        filter.setAuthenticationManager(authenticationManager(authenticationConfiguration));
        return filter;
    }

}