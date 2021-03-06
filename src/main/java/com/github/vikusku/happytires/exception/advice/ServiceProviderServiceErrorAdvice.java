package com.github.vikusku.happytires.exception.advice;

import com.github.vikusku.happytires.exception.ServiceProviderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.github.vikusku.happytires.exception.advice.ErrorAdviceUtil.error;

@ControllerAdvice
public class ServiceProviderServiceErrorAdvice {

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return  error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler({ServiceProviderNotFoundException.class})
    public ResponseEntity<String> handleServiceProviderNotFoundException(ServiceProviderNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
