package com.github.vikusku.happytires.exception.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.github.vikusku.happytires.exception.advice.ErrorAdviceUtil.error;

@ControllerAdvice
public class TimeSlotServiceErrorAdvice {

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return  error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler({InvalidUpdateDayAvailabilityRequest.class})
    public ResponseEntity<String> handleInvalidUpdateDayAvailabilityRequest(InvalidUpdateDayAvailabilityRequest e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }
}
