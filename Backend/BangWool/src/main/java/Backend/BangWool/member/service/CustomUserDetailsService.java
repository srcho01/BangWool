package Backend.BangWool.member.service;

import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.CustomUserDetails;
import Backend.BangWool.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 조회
        MemberEntity memberEntity = memberRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return new CustomUserDetails(memberEntity);
    }

    public boolean validateGoogle(String username, String googleId) {

        MemberEntity memberEntity = memberRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return googleId.equals(memberEntity.getGoogleId());
    }

    public boolean validateKakao(String username, String kakaoId) {

        MemberEntity memberEntity = memberRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return kakaoId.equals(memberEntity.getKakaoId());
    }

}