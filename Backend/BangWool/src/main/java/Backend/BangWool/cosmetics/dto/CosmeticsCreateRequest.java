package Backend.BangWool.cosmetics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Schema(description = "Cosmetics create request DTO")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CosmeticsCreateRequest {

    @Schema(example = "화장품 이름")
    @NotEmpty(message = "Name is required")
    private String name;

    @Schema(example = "2024-08-19", description = "형식 YYYY-MM-DD")
    @NotNull(message = "Expiration date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;

    @Schema(example = "basic", description = "basic, base, color, others 중 하나")
    @NotEmpty(message = "Category is required")
    private String category;

    @Schema(example = "화장대 위", description = "10자 이내")
    @NotEmpty(message = "location is required")
    private String location;

}
