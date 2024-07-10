package Backend.BangWool.member.service;

import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.LocalSignUpRequestDto;
import Backend.BangWool.member.repository.MemberRepository;
import Backend.BangWool.util.GlobalConstant;
import Backend.BangWool.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SignUpService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RedisUtil redisUtil;

    public boolean localSignUp(LocalSignUpRequestDto data) {

        // member 중복 체크
        if (memberRepository.existsByEmail(data.getEmail()))
            throw new BadRequestException("User is already registered.");

        // password 체크
        passwordCheck(data.getPassword1(), data.getPassword2());

        // nickname 체크
        boolean isNicknameCheck = nicknameCheck(data.getNickname());
        if (!isNicknameCheck)
            throw new BadRequestException("Nickname is already existed.");

        // Check email verification
        String isVerify = redisUtil.getData(GlobalConstant.REDIS_EMAIL_VERIFY + data.getEmail());
        if (isVerify == null)
            throw new BadRequestException("Need to verify your email first.");

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
        redisUtil.deleteData(GlobalConstant.REDIS_EMAIL_CODE);
        redisUtil.deleteData(GlobalConstant.REDIS_EMAIL_VERIFY);

        return true;
    }

    private void passwordCheck(String pw1, String pw2) {
        if (!pw1.equals(pw2))
            throw new BadRequestException("Passwords do not match.");

        String regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pw1);

        if (!matcher.matches())
            throw new BadRequestException("The password must be 8 to 20 characters, including all English, numbers, and special characters.");

    }

    public boolean nicknameCheck(String nickname) {

        if (nickname == null || nickname.isEmpty())
            throw new BadRequestException("Nickname is empty.");

        String regex = "^[가-힣a-zA-Z0-9]{1,10}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(nickname);

        if (!matcher.matches())
            throw new BadRequestException("The nickname must be 1 to 10 characters, consisting only of English, numbers, and Korean.");

        return !memberRepository.existsByNickname(nickname);
    }
}
