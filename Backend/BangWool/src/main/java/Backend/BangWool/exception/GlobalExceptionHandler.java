package Backend.BangWool.exception;

import Backend.BangWool.response.StatusResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public StatusResponse handleBadRequestHandler(BadRequestException e) {
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
    public StatusResponse handleNotFoundHandler(NotFoundException e) {
        return StatusResponse.build(404, e);
    }

    @ExceptionHandler(ServerException.class)
    public StatusResponse handleServerHandler(ServerException e) {
        return StatusResponse.build(500, e);
    }

}