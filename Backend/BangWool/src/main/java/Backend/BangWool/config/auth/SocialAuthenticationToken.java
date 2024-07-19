package Backend.BangWool.config.auth;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class SocialAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final String googleId;
    private final String kakaoId;

    public SocialAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(principal, null, authorities);
        this.googleId = null;
        this.kakaoId = null;
    }

    public SocialAuthenticationToken(Object principal, String googleId, String kakaoId) {
        super(principal, null);
        this.googleId = googleId;
        this.kakaoId = kakaoId;
    }

}