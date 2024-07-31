package Backend.BangWool.member.controller;

import Backend.BangWool.config.TestSecurityConfig;
import Backend.BangWool.member.dto.ChangePasswordRequest;
import Backend.BangWool.member.dto.MemberInfoResponse;
import Backend.BangWool.member.service.UserProfileService;
import Backend.BangWool.response.DataResponse;
import Backend.BangWool.response.StatusResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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


    private static Stream<Arguments> invalidChangePassword() {
        return Stream.of(
                Arguments.of(null, "prev1234!!", "test1234!!", "test1234!!", "Email is required"),
                Arguments.of("test", "prev1234!!", "test1234!!", "test1234!!", "Email is out form"),
                Arguments.of("test@test.com", null, "test1234!!", "test1234!!", "previous password is Required"),
                Arguments.of("test@test.com", "prev1234!!", null, "test1234!!", "password is Required"),
                Arguments.of("test@test.com", "prev1234!!", "test1234!!", null, "password confirmation is Required")
        );
    }

    @DisplayName("비밀번호 변경 - 에러")
    @ParameterizedTest
    @MethodSource("invalidChangePassword")
    void changePasswordFail(String email, String prev, String pw1, String pw2, String message) throws Exception {
        // given
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .email(email)
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
    void changePasswordSuccess() throws Exception {
        // given
        String email = "test@test.com";
        String prev = "prev1234!!";
        String pw1 = "test1234!!";
        String pw2 = "test1234!!";

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .email(email)
                .prevPassword(prev)
                .password1(pw1)
                .password2(pw2)
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        // mocking
        when(userProfileService.changePassword(request)).thenReturn(true);

        // then
        String response = objectMapper.writeValueAsString(StatusResponse.of(200));
        mvc.perform(post("/user/password/change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(response));
    }


    @DisplayName("회원정보 조회 - 에러")
    @Test
    void getMemberInfoFail() throws Exception {
        // then
        String responseJson = objectMapper.writeValueAsString(StatusResponse.of(400, "Required parameter not found."));

        mvc.perform(get("/user/info"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseJson));
    }

    @DisplayName("회원정보 조회 - 성공")
    @Test
    void getMemberInfoSuccess() throws Exception {
        // given
        String email = "test@test.com";

        // when
        MemberInfoResponse response = MemberInfoResponse.builder()
                .email(email)
                .name("test")
                .nickname("test")
                .birth(LocalDate.of(2000, 1, 1))
                .build();
        when(userProfileService.getMemberInfo(email)).thenReturn(response);

        // then
        String responseJson = objectMapper.writeValueAsString(DataResponse.of(response));
        mvc.perform(get("/user/info")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));
    }

}
