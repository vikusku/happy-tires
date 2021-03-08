package com.github.vikusku.happytires.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TimeSlotPK implements Serializable {
    private LocalDateTime start;
    private Long serviceProvider;
}
