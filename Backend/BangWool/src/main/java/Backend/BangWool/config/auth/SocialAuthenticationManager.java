package Backend.BangWool.config.auth;

import Backend.BangWool.member.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public class SocialAuthenticationManager implements AuthenticationManager {

    private final CustomUserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof SocialAuthenticationToken) {
            String username = (String) authentication.getPrincipal();
            String googleId = ((SocialAuthenticationToken) authentication).getGoogleId();
            String kakaoId = ((SocialAuthenticationToken) authentication).getKakaoId();

            if (googleId != null && kakaoId == null) { // google로 로그인 되었을 때
                if (userDetailsService.validateGoogle(username, googleId)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    return new SocialAuthenticationToken(userDetails, userDetails.getAuthorities());
                } else {
                    throw new AuthenticationException("Invalid Social ID") {};
                }
            } else if (googleId == null && kakaoId != null) { // kakao로 로그인 되었을 때
                if (userDetailsService.validateKakao(username, kakaoId)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    return new SocialAuthenticationToken(userDetails, userDetails.getAuthorities());
                } else {
                    throw new AuthenticationException("Invalid Social ID") {};
                }
            } else {
                throw new AuthenticationException("Invalid Social ID") {};
            }
        }
        return null;
    }

}
