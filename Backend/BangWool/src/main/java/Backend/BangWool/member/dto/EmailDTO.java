package Backend.BangWool.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailDTO {

    @Email(message = "Email is out of form")
    @NotEmpty(message = "Email is required")
    private String email;

    private String code;

}
