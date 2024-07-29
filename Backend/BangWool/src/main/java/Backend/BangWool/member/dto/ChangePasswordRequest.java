package Backend.BangWool.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Schema(description = "Change Password Request DTO")
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest extends SetPasswordRequest {

    @Schema(example = "prev1234!!")
    @NotEmpty(message = "previous password is Required")
    String prevPassword;

}
