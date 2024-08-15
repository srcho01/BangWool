package Backend.BangWool.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Schema(description = "Change Password Request DTO")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {

    @Schema(example = "prev1234!!")
    @NotEmpty(message = "previous password is Required")
    String prevPassword;

    @Schema(example = "test1234!!")
    @NotEmpty(message = "password is Required")
    private String password1;
    @Schema(example = "test1234!!")
    @NotEmpty(message = "password confirmation is Required")
    private String password2;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChangePasswordRequest request)) return false;
        return Objects.equals(prevPassword, request.prevPassword) && Objects.equals(password1, request.password1) && Objects.equals(password2, request.password2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prevPassword, password1, password2);
    }
}