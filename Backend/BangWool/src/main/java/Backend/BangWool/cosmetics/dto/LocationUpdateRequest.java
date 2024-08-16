package Backend.BangWool.cosmetics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Schema(description = "Option name update DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationUpdateRequest {

    @Schema(example = """
            {
                "옵션1 전": "옵션1 후",
                "옵션2 전": "옵션2 후",
            }""")
    Map<String, String> options;

}
