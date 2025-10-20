package com.devsuperior.dsmovie.handlers;

import java.util.ArrayList;

import com.devsuperior.dsmovie.dto.FieldMessageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.devsuperior.dsmovie.dto.CustomErrorDTO;
import com.devsuperior.dsmovie.dto.ValidationErrorDTO;
import com.devsuperior.dsmovie.exceptions.DatabaseException;
import com.devsuperior.dsmovie.exceptions.ResourceNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

import static java.time.Instant.now;
import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomErrorDTO> resourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        HttpStatus status = NOT_FOUND;
        CustomErrorDTO err = new CustomErrorDTO(
                now(),
                status.value(),
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<CustomErrorDTO> database(DatabaseException e, HttpServletRequest request) {
        HttpStatus status = BAD_REQUEST;
        CustomErrorDTO err = new CustomErrorDTO(
                now(),
                status.value(),
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorDTO> methodArgumentNotValidation(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        HttpStatus status = UNPROCESSABLE_ENTITY;
        ValidationErrorDTO err = new ValidationErrorDTO(
                now(),
                status.value(),
                "Dados inválidos",
                request.getRequestURI(),
                new ArrayList<>()
        );
        for (FieldError f : e.getBindingResult().getFieldErrors()) {
            err.errors().add(
                    new FieldMessageDTO(
                            f.getField(),
                            f.getDefaultMessage()
                    )
            );
        }
        return ResponseEntity.status(status).body(err);
    }
}
