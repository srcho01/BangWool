package Backend.BangWool.member.service;

import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.exception.NotFoundException;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.MemberInfoResponse;
import Backend.BangWool.member.repository.MemberRepository;
import Backend.BangWool.util.CONSTANT;
import Backend.BangWool.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final RedisUtil redisUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional(readOnly = true)
    public String findEmail(String name, LocalDate birth) {

        MemberEntity member = memberRepository.findByNameAndBirth(name, birth)
                .orElseThrow(() -> new NotFoundException("No users have that name and birth."));

        return member.getEmail();
    }

    @Transactional(readOnly = true)
    public boolean sendEmailForPassword(String email, String name, LocalDate birth) {

        MemberEntity member = memberRepository.findByEmailAndNameAndBirth(email, name, birth)
                .orElseThrow(() -> new NotFoundException("No users have that email, name and birth."));

        emailService.sendEmail(member.getEmail());

        return true;
    }

    public boolean checkCodeForPassword(String email, String code) {
        return emailService.checkCode(email, code);
    }

    public boolean setNewPassword(String email, String pw1, String pw2) {

        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        emailVerficationCheck(email);
        passwordCheck(pw1, pw2);

        member.setPassword(bCryptPasswordEncoder.encode(pw1));
        memberRepository.save(member);

        return true;
    }

    public boolean changePassword(String email, String prevPassword, String newPassword1, String newPassword2) {

        MemberEntity member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!bCryptPasswordEncoder.matches(prevPassword, member.getPassword())) {
            throw new BadRequestException("The previous password does not match");
        }

        emailVerficationCheck(email);
        passwordCheck(newPassword1, newPassword2);

        member.setPassword(bCryptPasswordEncoder.encode(newPassword1));
        memberRepository.save(member);

        return true;
    }

    public MemberInfoResponse getMemberInfo(String email) {

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


    public void emailVerficationCheck(String email) {
        String key = CONSTANT.REDIS_EMAIL_VERIFY + email;
        redisUtil.getData(key).filter("true"::equals)
                .orElseThrow(()->new BadRequestException("Need to verify your email first."));
    }

    public void passwordCheck(String pw1, String pw2) {
        if (!pw1.equals(pw2))
            throw new BadRequestException("Passwords do not match.");

        String regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pw1);

        if (!matcher.matches())
            throw new BadRequestException("The password must be 8 to 20 characters, including all English, numbers, and special characters.");

    }

}
