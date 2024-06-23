package ru.practicum.shareit.error.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.shareit.error.AlreadyExistsException;
import ru.practicum.shareit.error.ElementAccessException;
import ru.practicum.shareit.error.EntityNotExistsExeption;
import ru.practicum.shareit.error.PermissionException;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionApiHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, UnsupportedOperationException.class})
    public ErrorResponse handleValidationExceptions(Throwable ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.toString());
        errorResponse.log();
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ElementAccessException.class})
    public ErrorResponse handleAvalaibilityExceptions(Throwable ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.toString());
        errorResponse.log();
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({EntityNotExistsExeption.class, PermissionException.class})
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

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Unknown state: " + ex.getValue());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleOthersExceptions(Throwable ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.toString());
        errorResponse.log();
        return errorResponse;
    }
}
