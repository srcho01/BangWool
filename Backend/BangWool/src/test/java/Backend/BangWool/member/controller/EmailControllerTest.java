package Backend.BangWool.member.controller;

import Backend.BangWool.config.TestSecurityConfig;
import Backend.BangWool.member.dto.EmailCheckRequest;
import Backend.BangWool.member.dto.EmailSendRequest;
import Backend.BangWool.member.service.EmailService;
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

import java.util.stream.Stream;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {EmailController.class})
@ContextConfiguration(classes = TestSecurityConfig.class)
class EmailControllerTest {

    @MockBean
    private EmailService emailService;

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("이메일 전송 성공")
    void mailSend() throws Exception {
        // given
        EmailSendRequest request = EmailSendRequest.builder()
                .email("test@gmail.com").build();
        String requestJson = objectMapper.writeValueAsString(request);

        // when
        doNothing().when(emailService).sendEmail(request.getEmail());

        // then
        String responseJson = objectMapper.writeValueAsString(StatusResponse.of(200));
        mvc.perform(post("/auth/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));
    }

    private static Stream<Arguments> invalidEmailProvider() {
        return Stream.of(
                Arguments.of(null, "Email is required"),
                Arguments.of("", "Email is required"),
                Arguments.of("잘못된이메일형식", "Email is out form")
        );
    }

    @DisplayName("이메일 전송 실패")
    @ParameterizedTest
    @MethodSource("invalidEmailProvider")
    void mailSendFail(String email, String message) throws Exception {
        // given
        EmailSendRequest request = EmailSendRequest.builder()
                .email(email)
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        // when
        doNothing().when(emailService).sendEmail(request.getEmail());

        // then
        String responseJson = objectMapper.writeValueAsString(StatusResponse.of(400, message));
        mvc.perform(post("/auth/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseJson));
    }

    @Test
    @DisplayName("코드 확인 성공")
    void mailCheckSuccess() throws Exception {
        // given
        EmailCheckRequest request = EmailCheckRequest.builder()
                .email("test@gmail.com")
                .code("TEST12")
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        // when
        when(emailService.checkCode(request.getEmail(), request.getCode())).thenReturn(true);

        // then
        String responseJson = objectMapper.writeValueAsString(DataResponse.of(true));
        System.out.println(responseJson);
        mvc.perform(post("/auth/email/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));

    }

    @Test
    @DisplayName("다른 코드 전송")
    void mailCheckFail() throws Exception {
        // given
        EmailCheckRequest request = EmailCheckRequest.builder()
                .email("test@gmail.com")
                .code("TEST12")
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        // when
        when(emailService.checkCode(request.getEmail(), request.getCode())).thenReturn(false);

        // then
        String responseJson = objectMapper.writeValueAsString(DataResponse.of(false));
        System.out.println(responseJson);
        mvc.perform(post("/auth/email/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(responseJson));

    }

    private static Stream<Arguments> invalidCodeProvider() {
        return Stream.of(
                Arguments.of("잘못된이메일형식", "123456", "Email is out form"),
                Arguments.of(null, "123456", "Email is required"),
                Arguments.of("test@test.com", null, "Code is required")
        );
    }

    @ParameterizedTest
    @DisplayName("코드 확인 잘못된 형식")
    @MethodSource("invalidCodeProvider")
    void mailCheckFail(String email, String code, String message) throws Exception {
        // given
        EmailCheckRequest request = EmailCheckRequest.builder()
                .email(email)
                .code(code)
                .build();
        String requestJson = objectMapper.writeValueAsString(request);

        // when
        when(emailService.checkCode(request.getEmail(), request.getCode())).thenReturn(false);

        // then
        String responseJson = objectMapper.writeValueAsString(StatusResponse.of(400, message));
        mvc.perform(post("/auth/email/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(responseJson));

    }
}
