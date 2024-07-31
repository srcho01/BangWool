package Backend.BangWool.member.service;

import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.exception.NotFoundException;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.*;
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
public class UserAccountService {

    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final RedisUtil redisUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    public boolean nicknameCheck(String nickname) {

        String regex = "^[가-힣a-zA-Z0-9]{1,10}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(nickname);

        if (!matcher.matches())
            throw new BadRequestException("The nickname must be 1 to 10 characters, consisting only of English, numbers, and Korean.");

        if (memberRepository.existsByNickname(nickname))
            throw new BadRequestException("Nickname is already existed.");

        return true;
    }

    @Transactional(readOnly = true)
    public String findEmail(String name, LocalDate birth) {

        MemberEntity member = memberRepository.findByNameAndBirth(name, birth)
                .orElseThrow(() -> new NotFoundException("No users have that name and birth."));

        return member.getEmail();
    }

    @Transactional(readOnly = true)
    public boolean sendEmailForPassword(EmailSendForPasswordRequest request) {

        MemberEntity member = memberRepository.findByEmailAndNameAndBirth(request.getEmail(), request.getName(), request.getBirth())
                .orElseThrow(() -> new NotFoundException("No users have that email, name and birth."));

        emailService.sendEmail(EmailSendRequest.builder().email(member.getEmail()).build());

        return true;
    }

    public boolean checkCodeForPassword(EmailCheckRequest request) {

        return emailService.checkCode(EmailCheckRequest.builder().email(request.getEmail()).code(request.getCode()).build());

    }

    public boolean setNewPassword(SetPasswordRequest request) {

        MemberEntity member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        emailVerficationCheck(request.getEmail());
        passwordCheck(request.getPassword1(), request.getPassword2());

        member.setPassword(bCryptPasswordEncoder.encode(request.getPassword1()));
        memberRepository.save(member);

        return true;
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
