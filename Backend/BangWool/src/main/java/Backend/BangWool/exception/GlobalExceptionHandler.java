package Backend.BangWool.exception;

import Backend.BangWool.util.CustomResponse;
import Backend.BangWool.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<CustomResponse> handleBadRequestHandler(BadRequestException e) {
        return ResponseUtil.build(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().isEmpty() ?
                "Invalid arguments" :
                e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return ResponseUtil.build(HttpStatus.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<CustomResponse> handleNotFoundHandler(NotFoundException e) {
        return ResponseUtil.build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<CustomResponse> handleServerHandler(ServerException e) {
        return ResponseUtil.build(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

}