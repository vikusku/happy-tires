package com.github.vikusku.happytires.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Reservation reservation;
    private ServiceProvider serviceProvider;
}
