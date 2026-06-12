package com.example.bankcards.exception.handler;

import com.example.bankcards.dto.error.ErrorDtoOut;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDtoOut> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation exception: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String errorMessage = "Validation failed: " + String.join(", ", errors.values());

        ErrorDtoOut errorDtoOut = new ErrorDtoOut(
                HttpStatus.BAD_REQUEST.value(),
                "400 BAD_REQUEST",
                errorMessage,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorDtoOut, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorDtoOut> handleResponseStatusException(ResponseStatusException ex) {
        log.error("ResponseStatusException: {}", ex.getReason(), ex);

        ErrorDtoOut errorDtoOut = new ErrorDtoOut(
                ex.getStatusCode().value(),
                ex.getStatusCode().toString(),
                ex.getReason(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorDtoOut, ex.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDtoOut> handleAllExceptions(Exception ex) {
        log.error("Unhandled exception: ", ex);

        ErrorDtoOut errorDtoOut = new ErrorDtoOut(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorDtoOut, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
