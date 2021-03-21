package com.github.vikusku.happytires.dto;

import lombok.*;

import java.time.Duration;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class AvailabilityIntervalDto {
    private LocalTime start;
    private Duration durationMin;
}
