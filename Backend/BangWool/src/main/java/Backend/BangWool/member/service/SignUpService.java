package Backend.BangWool.member.service;

import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.LocalSignUpRequest;
import Backend.BangWool.member.dto.OAuthSignUpRequest;
import Backend.BangWool.member.repository.MemberRepository;
import Backend.BangWool.util.CONSTANT;
import Backend.BangWool.util.RedisUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RedisUtil redisUtil;
    private final UserAccountService userAccountService;

    @Transactional
    public boolean localSignUp(LocalSignUpRequest data) {

        // member 중복 체크
        if (memberRepository.existsByEmail(data.getEmail()))
            throw new BadRequestException("User is already registered.");

        // 이메일 인증 체크
        userAccountService.emailVerficationCheck(data.getEmail());
        // password 체크
        userAccountService.passwordCheck(data.getPassword1(), data.getPassword2());
        // nickname 체크
        userAccountService.nicknameCheck(data.getNickname());

        // entity로 변환
        MemberEntity memberEntity = MemberEntity.builder()
                .email(data.getEmail())
                .password(bCryptPasswordEncoder.encode(data.getPassword1()))
                .name(data.getName())
                .nickname(data.getNickname())
                .birth(data.getBirth())
                .build();

        // 저장
        memberRepository.save(memberEntity);

        // 가입 완료하여 인증코드, 인증여부 삭제
        redisUtil.deleteData(CONSTANT.REDIS_EMAIL_CODE);
        redisUtil.deleteData(CONSTANT.REDIS_EMAIL_VERIFY);

        return true;
    }

    @Transactional
    public boolean socialSignUp(OAuthSignUpRequest data) {

        // email 중복 체크
        if (memberRepository.existsByEmail(data.getEmail()))
            throw new BadRequestException("User is already registered.");

        // nickname 체크
        userAccountService.nicknameCheck(data.getNickname());

        // social id 체크
        String googleId = data.getGoogleId();
        String kakaoId = data.getKakaoId();
        checkSocialId(googleId, kakaoId);

        // entity로 변환
        MemberEntity memberEntity = MemberEntity.builder()
                .email(data.getEmail())
                .name(data.getName())
                .nickname(data.getNickname())
                .birth(data.getBirth())
                .googleId(googleId)
                .kakaoId(kakaoId)
                .build();

        // 저장
        memberRepository.save(memberEntity);

        return true;
    }

    private void checkSocialId(String googleId, String kakaoId) {
        // google, kakao ID 둘 다 없으면 Error
        if (googleId == null && kakaoId == null)
            throw new BadRequestException("You must have a social login for either Google or Kakao");

        // 중복 google ID, kakao ID 검사
        if ((googleId != null && memberRepository.existsByGoogleId(googleId)) || (kakaoId != null && memberRepository.existsByKakaoId(kakaoId)))
            throw new BadRequestException("This social account is already registered");
    }

}
