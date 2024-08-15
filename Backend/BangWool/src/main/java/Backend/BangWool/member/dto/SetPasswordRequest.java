package Backend.BangWool.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@Builder
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SetPasswordRequest request)) return false;
        return Objects.equals(email, request.email) && Objects.equals(password1, request.password1) && Objects.equals(password2, request.password2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password1, password2);
    }
}
