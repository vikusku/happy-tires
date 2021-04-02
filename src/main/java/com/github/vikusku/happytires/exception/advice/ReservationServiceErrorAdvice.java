package com.github.vikusku.happytires.exception.advice;

import com.github.vikusku.happytires.exception.ReservationPersistenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.github.vikusku.happytires.exception.advice.ErrorAdviceUtil.error;

@ControllerAdvice
public class ReservationServiceErrorAdvice {

    @ExceptionHandler({ReservationPersistenceException.class})
    public ResponseEntity<String> handleReservationPersistenceException(ReservationPersistenceException e) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
