package Backend.BangWool.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.LocalDate;

@Schema(description = "Member Information Response DTO")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberInfoResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "test@test.com")
    private String email;

    @Schema(example = "name")
    private String name;

    @Schema(example = "nickname")
    private String nickname;

    @Schema(example = "2000-01-01")
    private LocalDate birth;

    @Schema(example = "jfidksjg")
    private String googleId;

    @Schema(example = "4951351")
    private String kakaoId;

    @Schema(example = "https://bangwool-images.s3.ap-northeast-2.amazonaws.com/default-profile.jpg")
    private URI profileUrl;

}
