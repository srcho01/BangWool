package Backend.BangWool.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;


@Schema(description = "Change Member Info DTO")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeMemberInfoRequest {

    @Schema(example = "방울이")
    @NotEmpty(message = "Nickname is Required")
    private String nickname;

    @Schema(example = "fajwopfjf2j09fjsvj0")
    private String googleId;

    @Schema(example = "913120654016")
    private String kakaoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChangeMemberInfoRequest that)) return false;
        return Objects.equals(nickname, that.nickname) && Objects.equals(googleId, that.googleId) && Objects.equals(kakaoId, that.kakaoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickname, googleId, kakaoId);
    }
}
