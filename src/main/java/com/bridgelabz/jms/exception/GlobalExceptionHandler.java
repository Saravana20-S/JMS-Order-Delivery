package com.bridgelabz.jms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<?> handleOrderNotFound(OrderNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        Map.of(
                                "timestamp",
                                LocalDateTime.now(),

                                "status",
                                404,

                                "message",
                                exception.getMessage()
                        )
                );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException exception) {

        return ResponseEntity
                .badRequest()
                .body(
                        Map.of(
                                "timestamp",
                                LocalDateTime.now(),

                                "status",
                                400,

                                "message",
                                exception.getMessage()
                        )
                );
    }
}