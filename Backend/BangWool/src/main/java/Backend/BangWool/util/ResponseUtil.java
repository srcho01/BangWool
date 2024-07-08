package Backend.BangWool.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    public static ResponseEntity<CustomResponse> build(HttpStatus status, String message) {
        CustomResponse cr = new CustomResponse(status.value(), message);
        return new ResponseEntity<>(cr, status);
    }

    public static ResponseEntity<CustomResponse> build(HttpStatus status) {
        return build(status, status.getReasonPhrase());
    }
}
