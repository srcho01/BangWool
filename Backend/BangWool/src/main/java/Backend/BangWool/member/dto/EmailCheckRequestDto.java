package Backend.BangWool.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Schema(description = "Email Request DTO")
@Getter
public class EmailCheckRequestDto {

    @Schema(example = "test@gmail.com")
    @Email(message = "Email is out form")
    @NotEmpty(message = "Email is required")
    private String email;

    @Schema(example = "OF5W05")
    private String code;

}
