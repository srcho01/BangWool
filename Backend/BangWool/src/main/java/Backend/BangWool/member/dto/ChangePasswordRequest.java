package Backend.BangWool.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Schema(description = "Change Password Request DTO")
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest extends SetPasswordRequest {

    @Schema(example = "prev1234!!")
    @NotEmpty(message = "previous password is Required")
    String prevPassword;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChangePasswordRequest that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(prevPassword, that.prevPassword);
    }

}
