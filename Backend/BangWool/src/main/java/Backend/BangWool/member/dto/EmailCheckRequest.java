package Backend.BangWool.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Schema(description = "Email Request DTO")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailCheckRequest {

    @Schema(example = "test@gmail.com")
    @Email(message = "Email is out form")
    @NotEmpty(message = "Email is required")
    private String email;

    @Schema(example = "OF5W05")
    @NotEmpty(message = "Code is required")
    private String code;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailCheckRequest request)) return false;
        return Objects.equals(email, request.email) && Objects.equals(code, request.code);
    }
}
