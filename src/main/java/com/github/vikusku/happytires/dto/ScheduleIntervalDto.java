package com.github.vikusku.happytires.dto;

import lombok.*;

import java.time.Duration;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ScheduleIntervalDto {
    private LocalTime start;
    private Duration durationMin;
    private ReservationDto reservationDto;
    private IntervalStatus status;
}
