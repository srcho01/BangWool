package Backend.BangWool.exception;

import Backend.BangWool.response.StatusResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.ConnectException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public StatusResponse handleBadRequest(BadRequestException e) {
        return StatusResponse.build(400, e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public StatusResponse handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getAllErrors().isEmpty() ?
                "Invalid arguments" :
                e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return StatusResponse.build(400, errorMessage);
    }

    @ExceptionHandler(NotFoundException.class)
    public StatusResponse handleNotFound(NotFoundException e) {
        return StatusResponse.build(404, e);
    }

    @ExceptionHandler(ServerException.class)
    public StatusResponse handleServer(ServerException e) {
        return StatusResponse.build(500, e);
    }

    @ExceptionHandler(RuntimeException.class)
    public StatusResponse handleRuntime(RuntimeException e) {
        return StatusResponse.build(500, e);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public StatusResponse handleMessageNotReadable(HttpMessageNotReadableException e) {
        return StatusResponse.build(400, "Message format is incorrect.");
    }

    @ExceptionHandler(ConnectException.class)
    public StatusResponse handleConnect(ConnectException e) {
        return StatusResponse.build(500, "Access to database has been denied");
    }

}