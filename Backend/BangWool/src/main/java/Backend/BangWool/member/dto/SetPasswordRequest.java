package Backend.BangWool.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SetPasswordRequest {

    @Schema(example = "test@gmail.com")
    @Email(message = "Email is out form")
    @NotEmpty(message = "Email is required")
    private String email;

    @Schema(example = "test1234!!")
    @NotEmpty(message = "password is Required")
    private String password1;
    @Schema(example = "test1234!!")
    @NotEmpty(message = "password confirmation is Required")
    private String password2;

}
