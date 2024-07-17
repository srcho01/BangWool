package Backend.BangWool.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token issue DTO")
public record TokenResponseDto(@Schema(example = "new_access_token") String accessToken,
                               @Schema(example = "600000") long accessExpiresIn,
                               @Schema(example = "new_refresh_token") String refreshToken,
                               @Schema(example = "86400000") long refreshExpiresIn) {

    public TokenResponseDto(String accessToken, long accessExpiresIn, String refreshToken, long refreshExpiresIn) {
        this.accessToken = accessToken;
        this.accessExpiresIn = accessExpiresIn;
        this.refreshToken = refreshToken;
        this.refreshExpiresIn = refreshExpiresIn;
    }

}
