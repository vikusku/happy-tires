package com.github.vikusku.happytires.exception.advice;

public class InvalidUpdateDayAvailabilityRequest extends RuntimeException {

    public InvalidUpdateDayAvailabilityRequest(String message) {
        super(message);
    }
}
