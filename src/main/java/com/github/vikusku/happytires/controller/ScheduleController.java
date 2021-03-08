package com.github.vikusku.happytires.controller;

import com.github.vikusku.happytires.dto.request.ScheduleTimeSlotRequest;
import com.github.vikusku.happytires.dto.response.TimeSlotAvailabilityResponse;
import com.github.vikusku.happytires.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/timeslots")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<List<TimeSlotAvailabilityResponse>> getAvailableTimeSlots(
            @Valid
            @FutureOrPresent
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime start,
            @Valid
            @FutureOrPresent
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime end) {

        return ResponseEntity.ok(scheduleService.findAllAvailableTimeSlots(start, end));
    }

    @PostMapping(path = "{/serviceProviderId}")
    public ResponseEntity<Void> createScheduleForServiceProvider(
        @PathVariable long serviceProviderId,
        @Valid @NotNull @Size(min = 1) @RequestBody LinkedHashMap<LocalDate, List<ScheduleTimeSlotRequest>> schedule) {

        scheduleService.addTimeSlotsForServiceProvider(serviceProviderId, schedule);
        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "{/serviceProviderId}")
    public ResponseEntity<Void> updateScheduleForServiceProvider(
            @PathVariable long serviceProviderId,
            @Valid @NotNull @Size(min = 1) @RequestBody LinkedHashMap<LocalDate, List<ScheduleTimeSlotRequest>> schedule) {

        scheduleService.updateTimeSlotsForServiceProvider(serviceProviderId, schedule);
        return ResponseEntity.ok().build();
    }
}
