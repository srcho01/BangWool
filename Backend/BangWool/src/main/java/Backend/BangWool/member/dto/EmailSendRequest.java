package Backend.BangWool.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class EmailSendRequest {

    @Schema(example = "test@gmail.com")
    @Email(message = "Email is out form")
    @NotEmpty(message = "Email is required")
    private String email;

}
