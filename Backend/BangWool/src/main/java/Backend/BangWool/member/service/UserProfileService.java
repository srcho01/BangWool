package Backend.BangWool.member.service;

import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.exception.NotFoundException;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.ChangeMemberInfo;
import Backend.BangWool.member.dto.ChangePasswordRequest;
import Backend.BangWool.member.dto.MemberInfoResponse;
import Backend.BangWool.member.dto.Session;
import Backend.BangWool.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserAccountService userAccountService;


    public boolean changePassword(Session session, ChangePasswordRequest request) {

        String email = session.getUsername();

        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!bCryptPasswordEncoder.matches(request.getPrevPassword(), member.getPassword())) {
            throw new BadRequestException("The previous password does not match");
        }

        userAccountService.emailVerficationCheck(email);
        userAccountService.passwordCheck(request.getPassword1(), request.getPassword2());

        member.setPassword(bCryptPasswordEncoder.encode(request.getPassword1()));
        memberRepository.save(member);

        return true;
    }

    public MemberInfoResponse getMemberInfo(Session session) {

        String email = session.getUsername();

        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return MemberInfoResponse.builder()
                .memberID(member.getMemberID())
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .birth(member.getBirth())
                .googleId(member.getGoogleId())
                .kakaoId((member.getKakaoId()))
                .build();
    }

    public MemberInfoResponse setMemberInfo(Session session, ChangeMemberInfo request) {

        String email = session.getUsername();

        if (request.getGoogleId() == null && request.getKakaoId() == null) {
            throw new BadRequestException("Member signed up for social membership cannot disconnect all social connections.");
        }

        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        member.setNickname(request.getNickname());
        member.setGoogleId(request.getGoogleId());
        member.setKakaoId(request.getKakaoId());

        memberRepository.save(member);

        return MemberInfoResponse.builder()
                .memberID(member.getMemberID())
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .birth(member.getBirth())
                .googleId(member.getGoogleId())
                .kakaoId((member.getKakaoId()))
                .build();
    }

}
