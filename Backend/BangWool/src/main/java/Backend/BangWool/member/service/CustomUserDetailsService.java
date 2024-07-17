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

        MemberEntity memberEntity = memberRepository.findByEmail(username); // 조회

        if (memberEntity == null)
            throw new UsernameNotFoundException(username);

        return new CustomUserDetails(memberEntity);
    }
}