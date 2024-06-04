package ru.practicum.shareit.error.handler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.error.*;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class ExceptionApiHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ErrorResponse handleValidationExceptions(Throwable ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.toString());
        errorResponse.log();
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ItemNotExistsExeption.class, OwnerDoesNotExixtsExeption.class,
            UserDoesNotExixtsException.class})
    public ErrorResponse handleNotFoundExceptions(RuntimeException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.toString());
        errorResponse.log();
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({AlreadyExistsException.class})
    public ErrorResponse handleConflictExceptions(RuntimeException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.toString());
        errorResponse.log();
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({PermissionException.class})
    public ErrorResponse handlePermissionException(RuntimeException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.toString());
        errorResponse.log();
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleOthersExceptions(Throwable ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.toString());
        errorResponse.log();
        return errorResponse;
    }
}
