package Backend.BangWool.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Objects;

@Schema(description = "Email Request for finding password DTO")
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class EmailSendForPasswordRequest extends EmailSendRequest {

    @Schema(example = "김방울")
    @NotEmpty(message = "Name is Required")
    private String name;

    @Schema(example = "2000-01-01", description = "형식 YYYY-MM-DD")
    @NotNull(message = "Birth is Required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birth;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmailSendForPasswordRequest request)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(name, request.name) && Objects.equals(birth, request.birth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, birth);
    }
}
