package Backend.BangWool.cosmetics.dto;

import Backend.BangWool.cosmetics.domain.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.LocalDate;

@Schema(description = "Cosmetics Information response DTO")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CosmeticsInfoResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "3")
    private Long memberId;

    @Schema(example = "test@test.com")
    private String memberEmail;

    @Schema(example = "name")
    private String name;

    @Schema(example = "base")
    private Category category;

    @Schema(example = "2000-01-01")
    private LocalDate expirationDate;

    @Schema(example = "2000-05-01")
    private LocalDate startDate;

    @Schema(example = "0")
    private int status;

    @Schema(example = "화장대 위")
    private String locationName;

    @Schema(example = "https://bangwool-images.s3.ap-northeast-2.amazonaws.com/default-base.jpg")
    private URI image;

}
