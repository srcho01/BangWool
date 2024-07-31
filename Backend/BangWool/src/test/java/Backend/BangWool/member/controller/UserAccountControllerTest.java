package Backend.BangWool.member.controller;

import Backend.BangWool.config.TestSecurityConfig;
import Backend.BangWool.member.dto.*;
import Backend.BangWool.member.service.UserAccountService;
import Backend.BangWool.response.DataResponse;
import Backend.BangWool.response.StatusResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserAccountController.class})
@ContextConfiguration(classes = TestSecurityConfig.class)
public class UserAccountControllerTest {

    @MockBean
    private UserAccountService userAccountService;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;


    @DisplayName("닉네임 확인 - 실패 : 닉네임 null")
    @Test
    void nicknameCheckFail() throws Exception {
        // then
        String responseJson = objectMapper.writeValueAsString(StatusResponse.of(400, "Required parameter not found."));
        mvc.perform(get("/auth/nickname-check"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseJson));

    }

    @DisplayName("닉네임 확인 - 성공")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void nicknameCheckSuccess(boolean nicknameCheckReturn) throws Exception {
        // given
        String nickname = "test";

        // when
        when(userAccountService.nicknameCheck(nickname)).thenReturn(nicknameCheckReturn);

        // then
        String responseJson = objectMapper.writeValueAsString(DataResponse.of(nicknameCheckReturn));
        mvc.perform(get("/auth/nickname-check")
                        .param("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));

    }

    @DisplayName("아이디 찾기 - 에러")
    @Test
    void findEmailFail() throws Exception {
        // then
        String responseJson = objectMapper.writeValueAsString(StatusResponse.of(400, "Required parameter not found."));

        mvc.perform(get("/auth/lost/id")
                        .param("name", "test"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseJson));

        mvc.perform(get("/auth/lost/id")
                        .param("birth", "2000-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseJson));

        mvc.perform(get("/auth/lost/id")
                        .param("name", "test")
                        .param("birth", "20000612"))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("아이디 찾기 - 성공")
    @Test
    void findEmailSuccess() throws Exception {
        // given
        String email = "test@test.com";
        String name = "test";
        LocalDate birth = LocalDate.of(2000, 9, 12);

        // when
        when(userAccountService.findEmail(name, birth)).thenReturn(email);

        // then
        String responseJson = objectMapper.writeValueAsString(DataResponse.of(email));

        mvc.perform(get("/auth/lost/id")
                        .param("name", name)
                        .param("birth", birth.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));
    }


    private static Stream<Arguments> invalidSendEmail() {
        return Stream.of(
                Arguments.of(null, "test", LocalDate.of(2000, 5, 25)),
                Arguments.of("test@test.com", null, LocalDate.of(2000, 5, 25)),
                Arguments.of("test@test.com", "test", null)
        );
    }

    @DisplayName("비밀번호 찾기 이메일 전송 - 에러")
    @ParameterizedTest
    @MethodSource("invalidSendEmail")
    void sendEmailFail(String email, String name, LocalDate birth) throws Exception {
        // given
        EmailSendForPasswordRequest dto = EmailSendForPasswordRequest.builder()
                .email(email)
                .name(name)
                .birth(birth)
                .build();
        String request = objectMapper.writeValueAsString(dto);

        // then
        mvc.perform(post("/auth/lost/password/email-send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @DisplayName("비밀번호 찾기 이메일 전송 - 성공")
    @Test
    void sendEmailSuccess() throws Exception {
        // given
        EmailSendForPasswordRequest dto = EmailSendForPasswordRequest.builder()
                .email("test@test.com")
                .name("test")
                .birth(LocalDate.of(2000, 9, 23))
                .build();
        String request = objectMapper.writeValueAsString(dto);

        // when
        when(userAccountService.sendEmailForPassword(dto)).thenReturn(true);

        // then
        String response = objectMapper.writeValueAsString(StatusResponse.of(200));
        mvc.perform(post("/auth/lost/password/email-send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().json(response));
    }


    private static Stream<Arguments> invalidCodeProvider() {
        return Stream.of(
                Arguments.of("잘못된이메일형식", "123456", "Email is out form"),
                Arguments.of(null, "123456", "Email is required"),
                Arguments.of("test@test.com", null, "Code is required")
        );
    }

    @ParameterizedTest
    @DisplayName("비밀번호 찾기 코드 확인 - 에러")
    @MethodSource("invalidCodeProvider")
    void mailCheckFail(String email, String code, String message) throws Exception {
        // given
        EmailCheckRequest request = EmailCheckRequest.builder()
                .email(email)
                .code(code)
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        // then
        String responseJson = objectMapper.writeValueAsString(StatusResponse.of(400, message));
        mvc.perform(post("/auth/lost/password/email-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseJson));

    }

    @DisplayName("비밀번호 찾기 코드 확인 - 성공/실패")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void mailCheckSuccess(boolean isMatch) throws Exception {
        // given
        EmailCheckRequest request = EmailCheckRequest.builder()
                .email("test@gmail.com")
                .code("TEST12")
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        // when
        when(userAccountService.checkCodeForPassword(request)).thenReturn(isMatch);

        // then
        String responseJson = objectMapper.writeValueAsString(DataResponse.of(isMatch));
        System.out.println(responseJson);
        mvc.perform(post("/auth/lost/password/email-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));

    }


    private static Stream<Arguments> invalidSetNewPassword() {
        return Stream.of(
                Arguments.of(null, "test1234!!", "test1234!!", "Email is required"),
                Arguments.of("test", "test1234!!", "test1234!!", "Email is out form"),
                Arguments.of("test@test.com", null, "test1234!!", "password is Required"),
                Arguments.of("test@test.com", "test1234!!", null, "password confirmation is Required")
        );
    }

    @DisplayName("비밀번호 찾기 새 비밀번호 설정 - 에러")
    @ParameterizedTest
    @MethodSource("invalidSetNewPassword")
    void setNewPasswordFail(String email, String pw1, String pw2, String message) throws Exception {
        // given
        SetPasswordRequest request = SetPasswordRequest.builder()
                .email(email)
                .password1(pw1)
                .password2(pw2)
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        // then
        String response = objectMapper.writeValueAsString(StatusResponse.of(400, message));
        mvc.perform(post("/auth/lost/password/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(response));
    }

    @DisplayName("비밀번호 찾기 새 비밀번호 설정 - 성공")
    @Test
    void setNewPasswordSuccess() throws Exception {
        // given
        String email = "test@test.com";
        String pw1 = "test1234!!";
        String pw2 = "test1234!!";

        SetPasswordRequest request = SetPasswordRequest.builder()
                .email(email)
                .password1(pw1)
                .password2(pw2)
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        // mocking
        when(userAccountService.setNewPassword(request)).thenReturn(true);

        // then
        String response = objectMapper.writeValueAsString(StatusResponse.of(200));
        mvc.perform(post("/auth/lost/password/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(response));
    }

}
