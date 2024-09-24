package Backend.BangWool.cosmetics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "Cosmetics create request DTO")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CosmeticsUpdateRequest {

    @Schema(example = "1")
    @NotEmpty(message = "cosmetics id is required")
    private long id;

    @Schema(example = "화장품 이름", nullable = true)
    private String name;

    @Schema(example = "basic", nullable = true, description = "basic, base, color, others 중 하나")
    private String category;

    @Schema(example = "화장대 위", nullable = true, description = "10자 이내")
    private String location;

    @Schema(example = "UPDATE", nullable = true, description = "UPDATE, DELETE, null 중 하나")
    private String imageStatus;

}
