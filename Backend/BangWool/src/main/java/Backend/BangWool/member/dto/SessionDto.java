package Backend.BangWool.member.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
public class SessionDto implements UserDetails {

    // 유저의 이메일이 들어간다
    // 유저의 이메일은 중복 불가능하다
    private int id;
    private String username;
    private String role;

    public SessionDto(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return "ROLE_USER";
            }
        });

        return collection;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
