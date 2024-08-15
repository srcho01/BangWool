package Backend.BangWool.member.service;

import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.exception.NotFoundException;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.ChangeMemberInfo;
import Backend.BangWool.member.dto.ChangePasswordRequest;
import Backend.BangWool.member.dto.MemberInfoResponse;
import Backend.BangWool.member.dto.Session;
import Backend.BangWool.member.repository.MemberRepository;
import Backend.BangWool.util.CONSTANT;
import Backend.BangWool.util.RedisUtil;
import Backend.BangWool.util.WithMockMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@WithMockMember
public class UserProfileServiceTest {

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private RedisUtil redisUtil;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserProfileService userProfileService;

    private Session session;

    @BeforeEach
    void setup() {
        this.session = (Session) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


    private static Stream<Arguments> invalidChangePassword() {
        return Stream.of(
                Arguments.of(false, false, "test1234!!", "test1234!!", "test1234!!"),
                Arguments.of(true, false, "test1234!!", "test1234!!", "test1234!!"),
                Arguments.of(true, true, "test1234@@", "test1234!!", "test1234!!"), // 이전 비밀번호 불일치
                Arguments.of(true, true, "test1234!!", "test1234!!", "different"),
                Arguments.of(true, true, "test1234!!", "test!!", "test!!"),
                Arguments.of(true, true, "test1234!!", "test0!", "test0!")
        );
    }

    @DisplayName("비밀번호 변경 - 실패")
    @ParameterizedTest
    @MethodSource("invalidChangePassword")
    void changePasswordFail(boolean isMemberExist, boolean isVerify, String prevPW, String newPW1, String newPW2) {
        // given
        String email = session.getUsername();
        String password = "test1234!!";
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .prevPassword(password)
                .password1(newPW1)
                .password2(newPW2)
                .build();

        // mocking
        MemberEntity member = MemberEntity.builder().email(email).password(bCryptPasswordEncoder.encode(prevPW)).build();

        when(memberRepository.findByEmail(email)).thenReturn(isMemberExist ? Optional.of(member) : Optional.empty());
        when(redisUtil.getData(CONSTANT.REDIS_EMAIL_VERIFY + email)).thenReturn(isVerify ? Optional.of("true") : Optional.empty());

        // when & then
        if (isMemberExist) {
            assertThrows(BadRequestException.class, () -> userProfileService.changePassword(session, request));
        } else {
            assertThrows(NotFoundException.class, () -> userProfileService.changePassword(session, request));
        }
    }

    @DisplayName("비밀번호 변경 - 성공")
    @Test
    void changePasswordSuccess() {
        // given
        String email = session.getUsername();
        String prevPW = "test1234!!";
        String newPW1 = "test123456!!";
        String newPW2 = "test123456!!";
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .prevPassword(prevPW)
                .password1(newPW1)
                .password2(newPW2)
                .build();

        // mocking
        MemberEntity member = MemberEntity.builder().email(email).password(bCryptPasswordEncoder.encode(prevPW)).build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(redisUtil.getData(CONSTANT.REDIS_EMAIL_VERIFY + email)).thenReturn(Optional.of("true"));

        // when
        boolean result = userProfileService.changePassword(session, request);

        // then
        verify(memberRepository, times(1)).findByEmail(email);
        verify(memberRepository, times(1)).save(any(MemberEntity.class));

        assertThat(result).isEqualTo(true);

    }


    @DisplayName("회원정보 조회 - 실패 : 없는 유저")
    @Test
    void getMemberInfoFail() {
        // given
        String email = session.getUsername();

        // mocking
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        NotFoundException e = assertThrows(NotFoundException.class, () -> userProfileService.getMemberInfo(session));
        assertThat(e.getMessage()).isEqualTo("User not found");
    }

    @DisplayName("회원정보 조회 - 성공")
    @Test
    void getMemberInfoSuccess() {
        // given
        String email = session.getUsername();

        // mocking
        MemberEntity member = MemberEntity.builder()
                .email(email)
                .password("1234")
                .name("test")
                .nickname("test")
                .birth(LocalDate.of(2000, 1, 1))
                .googleId("anfiuownen")
                .kakaoId("12198964732")
                .build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        // when
        MemberInfoResponse result = userProfileService.getMemberInfo(session);

        // then
        assertThat(result).isInstanceOf(MemberInfoResponse.class);
    }


    @DisplayName("회원정보 수정 - 실패 : 소셜가입자 소셜 아이디 모두 삭제")
    @Test
    void setMemberInfoFail() {
        // 수정 가능 항목 : 닉네임, 카카오아이디, 구글 아이디
        // given
        String email = session.getUsername();
        ChangeMemberInfo request = ChangeMemberInfo.builder()
                .nickname("test")
                .googleId(null)
                .build();

        // mocking
        MemberEntity member = MemberEntity.builder()
                .email(email)
                .name("test")
                .nickname("test")
                .birth(LocalDate.of(2000, 1, 1))
                .googleId("wnefpivnjwofi")
                .build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        // when & then
        BadRequestException e = assertThrows(BadRequestException.class, () -> userProfileService.setMemberInfo(session, request));
        assertThat(e.getMessage()).isEqualTo("Member signed up for social membership cannot disconnect all social connections.");
    }

    @DisplayName("회원정보 수정 - 성공")
    @Test
    void setMemberInfoSuccess() {
        // 수정 가능 항목 : 닉네임, 카카오아이디, 구글 아이디
        // given
        String email = session.getUsername();
        ChangeMemberInfo request = ChangeMemberInfo.builder()
                .nickname("test")
                .googleId("wnefpivnjwofi")
                .build();

        // mocking
        MemberEntity member = MemberEntity.builder()
                .email(email)
                .password("1234")
                .name("test")
                .nickname("test")
                .birth(LocalDate.of(2000, 1, 1))
                .googleId("wnefpivnjwofi")
                .build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        // when
        MemberInfoResponse result = userProfileService.setMemberInfo(session, request);

        // then
        assertThat(result).isInstanceOf(MemberInfoResponse.class);
    }


    @DisplayName("회원 탈퇴 - 실패")
    @Test
    void withdrawalFail() {
        // given
        Long id = session.getId();

        // when & then
        NotFoundException e = assertThrows(NotFoundException.class, () -> userProfileService.withdrawal(session));
        assertThat(e.getMessage()).isEqualTo("Member with id " + id + " not found");
    }

    @DisplayName("회원 탈퇴 - 성공")
    @Test
    void withdrawalSuccess() {
        // given
        Long id = session.getId();

        // mocking
        when(memberRepository.existsById(session.getId())).thenReturn(true);

        // when
        userProfileService.withdrawal(session);

        // then
        verify(memberRepository, times(1)).deleteById(id);
    }

}
