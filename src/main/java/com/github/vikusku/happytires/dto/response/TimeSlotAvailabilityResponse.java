package com.github.vikusku.happytires.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class TimeSlotAvailabilityResponse {
    private LocalTime start;
    private Status status;

    public enum Status {
        UNAVAILABLE,
        AVAILABLE,
        RESERVED
    }
}


