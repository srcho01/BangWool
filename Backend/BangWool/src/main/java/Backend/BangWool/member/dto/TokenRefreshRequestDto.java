package Backend.BangWool.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "Access Token reissue request DTO")
public record TokenRefreshRequestDto(
        @Schema(example = "refresh_token") @NotEmpty(message = "refresh token is required") String refreshToken) {
}
