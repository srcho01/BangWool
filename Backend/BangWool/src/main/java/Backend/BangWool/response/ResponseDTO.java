package Backend.BangWool.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResponseDTO {

    @Schema(example = "200")
    private final int code;

    @Schema(example = "OK")
    private final String message;

    public ResponseDTO(HttpStatus httpStatus, String message) {
        this.code = httpStatus.value();
        this.message = message;
    }

    public static ResponseDTO build(StatusCode statusCode) {
        return new ResponseDTO(statusCode.getStatus(), statusCode.getMessage());
    }
}