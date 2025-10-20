package com.devsuperior.dsmovie.handlers;


import com.devsuperior.dsmovie.dto.CustomErrorDTO;
import com.devsuperior.dsmovie.dto.FieldMessageDTO;
import com.devsuperior.dsmovie.dto.ValidationErrorDTO;
import com.devsuperior.dsmovie.exceptions.DatabaseException;
import com.devsuperior.dsmovie.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;

import static java.time.Instant.now;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomErrorDTO> resourceNotFound(
            ResourceNotFoundException resourceNotFoundException,
            HttpServletRequest httpServletRequest
    ) {
        HttpStatus status = NOT_FOUND;
        return ResponseEntity.status(status).body(
                new CustomErrorDTO(
                        now(),
                        status.value(),
                        resourceNotFoundException.getMessage(),
                        httpServletRequest.getRequestURI()
                )
        );
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<CustomErrorDTO> database(
            DatabaseException databaseException,
            HttpServletRequest httpServletRequest
    ) {
        HttpStatus status = BAD_REQUEST;
        return ResponseEntity.status(status).body(
                new CustomErrorDTO(
                        now(),
                        status.value(),
                        databaseException.getMessage(),
                        httpServletRequest.getRequestURI()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorDTO> beanValidation(
            MethodArgumentNotValidException methodArgumentNotValidException,
            HttpServletRequest httpServletRequest
    ) {
        HttpStatus status = UNPROCESSABLE_ENTITY;
        var err = new ValidationErrorDTO(
                now(),
                status.value(),
                "Dados inválidos",
                httpServletRequest.getRequestURI(),
                new ArrayList<>()
        );
        for (FieldError fieldError : methodArgumentNotValidException.getBindingResult().getFieldErrors()) {
            err.errors().add(
                    new FieldMessageDTO(
                            fieldError.getField(),
                            fieldError.getDefaultMessage()
                    )
            );
        }
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            ConstraintViolationException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ValidationErrorDTO> unreadable(HttpServletRequest httpServletRequest) {
        HttpStatus status = UNPROCESSABLE_ENTITY;
        var err = new ValidationErrorDTO(
                now(), status.value(),
                "Dados inválidos",
                httpServletRequest.getRequestURI(),
                new ArrayList<>()
        );
        return ResponseEntity.status(status).body(err);
    }
}
