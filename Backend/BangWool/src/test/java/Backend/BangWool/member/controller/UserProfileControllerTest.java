package Backend.BangWool.member.controller;

import Backend.BangWool.config.TestSecurityConfig;
import Backend.BangWool.member.dto.ChangeMemberInfo;
import Backend.BangWool.member.dto.ChangePasswordRequest;
import Backend.BangWool.member.dto.MemberInfoResponse;
import Backend.BangWool.member.dto.Session;
import Backend.BangWool.member.service.UserProfileService;
import Backend.BangWool.response.DataResponse;
import Backend.BangWool.response.StatusResponse;
import Backend.BangWool.util.WithMockMember;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserProfileController.class})
@ContextConfiguration(classes = TestSecurityConfig.class)
public class UserProfileControllerTest {

    @MockBean
    private UserProfileService userProfileService;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    private Session session;

    @BeforeEach
    void setup() {
        this.session = (Session) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


    private static Stream<Arguments> invalidChangePassword() {
        return Stream.of(
                Arguments.of( null, "test1234!!", "test1234!!", "previous password is Required"),
                Arguments.of("prev1234!!", null, "test1234!!", "password is Required"),
                Arguments.of("prev1234!!", "test1234!!", null, "password confirmation is Required")
        );
    }

    @DisplayName("비밀번호 변경 - 에러")
    @ParameterizedTest
    @MethodSource("invalidChangePassword")
    @WithMockMember
    void changePasswordFail(String prev, String pw1, String pw2, String message) throws Exception {
        // given
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .prevPassword(prev)
                .password1(pw1)
                .password2(pw2)
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        // then
        String response = objectMapper.writeValueAsString(StatusResponse.of(400, message));
        mvc.perform(post("/user/password/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(response));
    }

    @DisplayName("비밀번호 변경 - 성공")
    @Test
    @WithMockMember
    void changePasswordSuccess() throws Exception {
        // given
        String prev = "prev1234!!";
        String pw1 = "test1234!!";
        String pw2 = "test1234!!";

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .prevPassword(prev)
                .password1(pw1)
                .password2(pw2)
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        // mocking
        when(userProfileService.changePassword(session, request)).thenReturn(true);

        // then
        String response = objectMapper.writeValueAsString(StatusResponse.of(200));
        mvc.perform(post("/user/password/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(response));
    }


    @DisplayName("회원정보 조회 - 성공")
    @Test
    @WithMockMember
    void getMemberInfoSuccess() throws Exception {
        // given
        String email = session.getUsername();

        // when
        MemberInfoResponse response = MemberInfoResponse.builder()
                .email(email)
                .name("test")
                .nickname("test")
                .birth(LocalDate.of(2000, 1, 1))
                .build();
        when(userProfileService.getMemberInfo(session)).thenReturn(response);

        // then
        String responseJson = objectMapper.writeValueAsString(DataResponse.of(response));
        mvc.perform(get("/user/info"))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));
    }


    @DisplayName("회원정보 수정 - 성공")
    @Test
    @WithMockMember
    void setMemberInfoFail() throws Exception {
        // given
        String email = session.getUsername();
        ChangeMemberInfo request = ChangeMemberInfo.builder()
                .nickname("newname")
                .googleId("google")
                .kakaoId("prev")
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        // when
        MemberInfoResponse response = MemberInfoResponse.builder()
                .email(email)
                .name("test")
                .nickname("newname")
                .birth(LocalDate.of(2000, 1, 1))
                .googleId("google")
                .kakaoId("prev")
                .build();
        when(userProfileService.setMemberInfo(session, request)).thenReturn(response);

        // then
        String responseJson = objectMapper.writeValueAsString(DataResponse.of(response));
        mvc.perform(post("/user/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));
    }

}
