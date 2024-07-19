package Backend.BangWool.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Schema(description = "소셜 회원가입 요청 DTO")
@Getter
@Builder
@AllArgsConstructor
public class OAuthSignUpRequestDto {

    @Schema(example = "test@gmail.com")
    @NotEmpty(message = "Email is Required")
    @Email(message = "Email is out form")
    private String email;

    @Schema(example = "김방울")
    @NotEmpty(message = "Name is Required")
    private String name;

    @Schema(example = "방울이")
    @NotEmpty(message = "Nickname is Required")
    private String nickname;

    @Schema(example = "2000-01-01", description = "형식 YYYY-MM-DD")
    @NotNull(message = "Birth is Required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birth;

    @Schema(example = "fajwopfjf2j09fjsvj0")
    private String google;

    @Schema(example = "913120654016")
    private Long kakao;

}
