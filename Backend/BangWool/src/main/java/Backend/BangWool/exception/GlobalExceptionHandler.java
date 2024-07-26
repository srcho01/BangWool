package Backend.BangWool.exception;

import Backend.BangWool.response.StatusResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.ConnectException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<StatusResponse> handleBadRequest(BadRequestException e) {
        StatusResponse response = StatusResponse.build(400, e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StatusResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().isEmpty() ?
                "Invalid arguments" :
                e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        StatusResponse response = StatusResponse.build(400, errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private String extractErrorMessage(MethodArgumentTypeMismatchException ex) {
        String parameterName = ex.getName();
        String parameterType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";
        return String.format("Parameter '%s' with value '%s' could not be converted to type '%s'", parameterName, value, parameterType);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<StatusResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        String parameterName = e.getName();
        String parameterType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown";
        String value = e.getValue() != null ? e.getValue().toString() : "null";
        String message = String.format("Parameter '%s' with value '%s' could not be converted to type '%s'", parameterName, value, parameterType);

        StatusResponse response = StatusResponse.build(400, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<StatusResponse> handleMissingParam(MissingServletRequestParameterException e) {
        StatusResponse response = StatusResponse.build(400, "Required parameter not found.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StatusResponse> handleMessageNotReadable(HttpMessageNotReadableException e) {
        StatusResponse response = StatusResponse.build(400, "Message format is incorrect.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<StatusResponse> handleNotFound(NotFoundException e) {
        StatusResponse response = StatusResponse.build(404, e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<StatusResponse> handleServer(ServerException e) {
        StatusResponse response = StatusResponse.build(500, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<StatusResponse> handleRuntime(RuntimeException e) {
        StatusResponse response = StatusResponse.build(500, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(ConnectException.class)
    public ResponseEntity<StatusResponse> handleConnect(ConnectException e) {
        StatusResponse response = StatusResponse.build(500, "Access to database has been denied");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}