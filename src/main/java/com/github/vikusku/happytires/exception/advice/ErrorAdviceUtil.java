package com.github.vikusku.happytires.exception.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ErrorAdviceUtil {

    public static ResponseEntity<String> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body((message));
    }
}
