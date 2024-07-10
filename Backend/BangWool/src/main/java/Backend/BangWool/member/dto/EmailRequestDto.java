package Backend.BangWool.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Email Request DTO")
@Getter
@Setter
public class EmailRequestDto {

    @Schema(example = "test@gmail.com")
    @Email(message = "Email is out form")
    @NotEmpty(message = "Email is required")
    private String email;

    @Schema(description = "/email/check에서만 필수", example = "OF5W05", nullable = true)
    private String code;

}
