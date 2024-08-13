package Backend.BangWool.member.service;

import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.exception.NotFoundException;
import Backend.BangWool.image.service.S3ImageService;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.ChangeMemberInfo;
import Backend.BangWool.member.dto.ChangePasswordRequest;
import Backend.BangWool.member.dto.MemberInfoResponse;
import Backend.BangWool.member.dto.Session;
import Backend.BangWool.member.repository.MemberRepository;
import Backend.BangWool.util.CONSTANT;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserAccountService userAccountService;
    private final S3ImageService s3ImageService;


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

        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (member.getPassword() == null && request.getGoogleId() == null && request.getKakaoId() == null) {
            throw new BadRequestException("Member signed up for social membership cannot disconnect all social connections.");
        }

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

    public void withdrawal(Session session) {
        if (!memberRepository.existsById(session.getId())) {
            throw new NotFoundException("Member with id " + session.getId() + " not found");
        }

        memberRepository.deleteById(session.getId());
    }

    public String profileUpload(Session session, MultipartFile image) {
        // get member entity
        int id = session.getId();
        MemberEntity member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String filename = "profile_" + id;
        URI uri = s3ImageService.upload(image, filename, 256,true);

        // 프로필 사진 기본 이미지로 변경
        member.setProfileImage(uri);
        memberRepository.save(member);

        return member.getProfileImage().toString();
    }

    public String profileDelete(Session session) {
        // get member entity
        int id = session.getId();
        MemberEntity member = memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // get uri key
        URI profileUri = member.getProfileImage();
        String key = s3ImageService.getKeyFromUrl(profileUri);

        // 이전에 설정한 프로필 이미지가 있다면 삭제
        if (!key.equals("default-profile.png")) {
            s3ImageService.delete(profileUri);
        }

        // 프로필 사진 기본 이미지로 변경
        member.setProfileImage(CONSTANT.DEFAULT_PROFILE);
        memberRepository.save(member);

        return member.getProfileImage().toString();
    }

}
