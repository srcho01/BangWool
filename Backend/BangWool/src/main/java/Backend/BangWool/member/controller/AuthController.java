package Backend.BangWool.member.controller;

import Backend.BangWool.response.DataResponse;
import Backend.BangWool.response.StatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authorization", description = "로그인, 로그아웃 등 사용자 인증 관련 API")
@RestController
public class AuthController {
    @Getter
    public static class LoginDTO {
        @Schema(example = "test@gmail.com")
        @NotEmpty
        private String email;

        @Schema(example = "test1234!!")
        @NotEmpty
        private String password;
    }

    @Getter
    public static class LogoutDTO {
        @Schema(example = "your_access_token")
        @NotEmpty
        private String accessToken;

        @Schema(example = "your_refresh_token")
        @NotEmpty
        private String refreshToken;
    }

    @Getter
    public static class OAuthLoginDTO {
        @Schema(example = "test@gmail.com")
        @NotEmpty
        private String email;

        @Schema(example = "efwhj8240qjf3")
        private String googleId;

        @Schema(example = "49513146413197")
        private String kakaoId;
    }

    @Operation(summary = "자체 Login")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DataResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\": \"200\", \"message\": \"OK\", \"data\": {\"accessToken\": \"new_access_token\", \"refreshToken\": \"new_refresh_token\"}}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 실패 시 응답",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\": \"401\", \"message\": \"Unauthorized\"}"
                            )
                    )
            )
    })
    @PostMapping("/login")
    public void login(@RequestBody LoginDTO loginDTO) {}


    @Operation(summary = "소셜 Login", description = "※ googleId와 kakaoId 중 하나만 있어야 합니다 ※")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DataResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\": \"200\", \"message\": \"OK\", \"data\": {\"accessToken\": \"new_access_token\", \"refreshToken\": \"new_refresh_token\"}}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인 실패 시 응답",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = "{\"code\": \"401\", \"message\": \"Unauthorized\"}"
                            )
                    )
            )
    })
    @PostMapping("/login/oauth")
    public void oauthLogin(@RequestBody OAuthLoginDTO loginDTO) {}


    @Operation(summary = "Logout", description = "Logout 요청이 오면, 기존 Access Token과 Refresh Token이 파기됩니다." +
            "\nAccess Token은 만료되어도 상관없으나 꼭 보내야합니다. Refresh Token은 만료되면 안됩니다.")
    @ApiResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = StatusResponse.class)
            )
    )
    @DeleteMapping("/logout")
    public void logout(@RequestBody LogoutDTO logoutDTO) {}

}