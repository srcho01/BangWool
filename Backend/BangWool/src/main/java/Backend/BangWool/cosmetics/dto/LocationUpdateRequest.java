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

    @Schema(example = "{\n" +
            "    \"옵션1 전\": \"옵션1 후\",\n" +
            "    \"옵션2 전\": \"옵션2 후\"\n" +
            "}")
    Map<String, String> options;

}
