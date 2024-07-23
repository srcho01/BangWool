package Backend.BangWool.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Token issue DTO")
public record TokenResponse(@Schema(example = "new_access_token") String accessToken,
                            @Schema(example = "600000") long accessExpiresIn,
                            @Schema(example = "new_refresh_token") String refreshToken,
                            @Schema(example = "86400000") long refreshExpiresIn) {

    @Builder
    public TokenResponse(String accessToken, long accessExpiresIn, String refreshToken, long refreshExpiresIn) {
        this.accessToken = accessToken;
        this.accessExpiresIn = accessExpiresIn;
        this.refreshToken = refreshToken;
        this.refreshExpiresIn = refreshExpiresIn;
    }

}
