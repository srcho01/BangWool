package Backend.BangWool.member.controller;

import Backend.BangWool.response.DataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    @Getter
    @Setter
    static class LoginDTO {
        @Schema(example = "srcho01@naver.com")
        private String email;

        @Schema(example = "test1234!!")
        private String password;
    }

    @Operation(summary = "자체 Login", tags = "Login")
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

}