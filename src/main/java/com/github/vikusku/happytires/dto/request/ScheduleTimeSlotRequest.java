package com.github.vikusku.happytires.dto.request;

import com.github.vikusku.happytires.dto.TimeSlotStatus;
import lombok.*;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class ScheduleTimeSlotRequest {
    private LocalTime start;
    private TimeSlotStatus status;
}
