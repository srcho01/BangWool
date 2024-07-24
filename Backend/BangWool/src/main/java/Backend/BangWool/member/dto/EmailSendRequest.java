package Backend.BangWool.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailSendRequest {

    @Schema(example = "test@gmail.com")
    @Email(message = "Email is out form")
    @NotEmpty(message = "Email is required")
    private String email;

}
