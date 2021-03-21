package com.github.vikusku.happytires.dto.response;

import com.github.vikusku.happytires.dto.ReservationDto;
import lombok.Data;

import java.time.LocalTime;

@Data
public class TimeSlotAvailabilityResponse {
    private LocalTime start;
    private Status status;
    private ReservationDto reservationDto;

    public enum Status {
        UNAVAILABLE,
        AVAILABLE,
        RESERVED
    }
}


