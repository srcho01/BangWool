package Backend.BangWool.member.controller;

import Backend.BangWool.config.TestSecurityConfig;
import Backend.BangWool.member.dto.LocalSignUpRequest;
import Backend.BangWool.member.dto.OAuthSignUpRequest;
import Backend.BangWool.member.service.SignUpService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {SignUpController.class})
@ContextConfiguration(classes = TestSecurityConfig.class)
class SignUpControllerTest {

    @MockBean
    private SignUpService signUpService;

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @DisplayName("닉네임 확인 - 실패 : 닉네임 null")
    @Test
    void nicknameCheckFail() throws Exception {
        // then
        String responseJson = objectMapper.writeValueAsString(StatusResponse.build(400, "Required parameter not found."));
        mvc.perform(get("/auth/signup/nickname-check"))
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
        when(signUpService.nicknameCheck(nickname)).thenReturn(nicknameCheckReturn);

        // then
        String responseJson = objectMapper.writeValueAsString(DataResponse.build(nicknameCheckReturn));
        mvc.perform(get("/auth/signup/nickname-check")
                        .param("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));

    }

    private static Stream<Arguments> invalidLocalSignUp() {
        return Stream.of(
                Arguments.of(LocalSignUpRequest.builder()
                        .email(null)
                        .password1("test1234!!").password2("test1234!!")
                        .name("test")
                        .nickname("test")
                        .birth(LocalDate.of(2000, 3, 12)).build(),
                        "Email is Required"),
                Arguments.of(LocalSignUpRequest.builder()
                        .email("test")
                        .password1("test1234!!").password2("test1234!!")
                        .name("test")
                        .nickname("test")
                        .birth(LocalDate.of(2000, 3, 12)).build(),
                        "Email is out form"),
                Arguments.of(LocalSignUpRequest.builder()
                        .email("test@test.com")
                        .password1(null).password2("test1234!!")
                        .name("test")
                        .nickname("test")
                        .birth(LocalDate.of(2000, 3, 12)).build(),
                        "password is Required"),
                Arguments.of(LocalSignUpRequest.builder()
                        .email("test@test.com")
                        .password1("test1234!!").password2(null)
                        .name("test")
                        .nickname("test")
                        .birth(LocalDate.of(2000, 3, 12)).build(),
                        "password confirmation is Required"),
                Arguments.of(LocalSignUpRequest.builder()
                        .email("test@test.com")
                        .password1("test1234!!").password2("test1234!!")
                        .name(null)
                        .nickname("test")
                        .birth(LocalDate.of(2000, 3, 12)).build(),
                        "Name is Required"),
                Arguments.of(LocalSignUpRequest.builder()
                        .email("test@test.com")
                        .password1("test1234!!").password2("test1234!!")
                        .name("test")
                        .nickname(null)
                        .birth(LocalDate.of(2000, 3, 12)).build(),
                        "Nickname is Required"),
                Arguments.of(LocalSignUpRequest.builder()
                        .email("test@test.com")
                        .password1("test1234!!").password2("test1234!!")
                        .name("test")
                        .nickname("test")
                        .birth(null).build(),
                        "Birth is Required")
        );
    }

    @DisplayName("자체 회원가입 - 실패 : 필드 중 null 존재 또는 형식 오류")
    @ParameterizedTest
    @MethodSource("invalidLocalSignUp")
    void localSignUpFail(LocalSignUpRequest request, String message) throws Exception {
        // given
        String requestJson = objectMapper.writeValueAsString(request);

        // then
        String responseJson = objectMapper.writeValueAsString(StatusResponse.build(400, message));
        mvc.perform(post("/auth/signup/local")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseJson));
    }

    @DisplayName("자체 회원가입 - 성공")
    @Test
    void localSignUpSuccess() throws Exception {
        // given
        LocalSignUpRequest request = LocalSignUpRequest.builder()
                .email("test@test.com")
                .password1("test1234!!").password2("test1234!!")
                .name("test")
                .nickname("test")
                .birth(LocalDate.of(2000, 3, 12)).build();
        String requestJson = objectMapper.writeValueAsString(request);

        // when
        when(signUpService.localSignUp(any(LocalSignUpRequest.class))).thenReturn(true);

        // then
        String responseJson = objectMapper.writeValueAsString(StatusResponse.build(200));
        mvc.perform(post("/auth/signup/local")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));

    }


    private static Stream<Arguments> invalidSocialSignUp() {
        return Stream.of(
                Arguments.of(OAuthSignUpRequest.builder()
                                .email(null)
                                .name("test")
                                .nickname("test")
                                .birth(LocalDate.of(2000, 3, 12))
                                .googleId("awofijlkfjn")
                                .kakaoId("984621685461").build(),
                        "Email is Required"),
                Arguments.of(OAuthSignUpRequest.builder()
                                .email("test")
                                .name("test")
                                .nickname("test")
                                .birth(LocalDate.of(2000, 3, 12))
                                .googleId("awofijlkfjn")
                                .kakaoId("984621685461").build(),
                        "Email is out form"),
                Arguments.of(OAuthSignUpRequest.builder()
                                .email("test@test.com")
                                .name(null)
                                .nickname("test")
                                .birth(LocalDate.of(2000, 3, 12))
                                .googleId("awofijlkfjn")
                                .kakaoId("984621685461").build(),
                        "Name is Required"),
                Arguments.of(OAuthSignUpRequest.builder()
                                .email("test@test.com")
                                .name("test")
                                .nickname(null)
                                .birth(LocalDate.of(2000, 3, 12))
                                .googleId("awofijlkfjn")
                                .kakaoId("984621685461").build(),
                        "Nickname is Required"),
                Arguments.of(OAuthSignUpRequest.builder()
                                .email("test@test.com")
                                .name("test")
                                .nickname("test")
                                .birth(null)
                                .googleId("awofijlkfjn")
                                .kakaoId("984621685461").build(),
                        "Birth is Required")
        );
    }

    @DisplayName("소셜 회원가입 - 실패 : 필드 중 null 존재 또는 형식 오류")
    @ParameterizedTest
    @MethodSource("invalidSocialSignUp")
    void socialSignUpFail(OAuthSignUpRequest request, String message) throws Exception {
        // given
        String requestJson = objectMapper.writeValueAsString(request);

        // then
        String responseJson = objectMapper.writeValueAsString(StatusResponse.build(400, message));
        mvc.perform(post("/auth/signup/oauth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseJson));
    }

    @DisplayName("소셜 회원가입 - 성공")
    @Test
    void socialSignUp() throws Exception {
        // given
        OAuthSignUpRequest request = OAuthSignUpRequest.builder()
                .email("test@test.com")
                .name("test")
                .nickname("test")
                .birth(LocalDate.of(2000, 3, 12))
                .googleId("awofijlkfjn")
                .kakaoId("984621685461")
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        // when
        when(signUpService.socialSignUp(any(OAuthSignUpRequest.class))).thenReturn(true);

        // then
        String responseJson = objectMapper.writeValueAsString(StatusResponse.build(200));
        mvc.perform(post("/auth/signup/oauth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));

    }
}