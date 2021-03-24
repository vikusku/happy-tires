package com.github.vikusku.happytires.controller;

import com.github.vikusku.happytires.dto.AvailabilityIntervalDto;
import com.github.vikusku.happytires.dto.ScheduleIntervalDto;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// TODO add tests
@RestController
@RequestMapping(path = "/api/v1/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping(path = "{/serviceProviderId}")
    public ResponseEntity<Map<LocalDate, List<ScheduleIntervalDto>>> getScheduleForServiceProvider(
            @PathVariable long serviceProviderId,
            @Valid
            @FutureOrPresent
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDate from,
            @Valid
            @FutureOrPresent
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDate until) {

        return ResponseEntity.ok(
                scheduleService.getScheduleForServiceProvider(serviceProviderId, from, until));
    }

    @PostMapping(path = "{/serviceProviderId}")
    public ResponseEntity<Void> createScheduleForServiceProvider(
        @PathVariable long serviceProviderId,
        @Valid @NotNull @Size(min = 1) @RequestBody LinkedHashMap<LocalDate, List<AvailabilityIntervalDto>> schedule) {

        scheduleService.createScheduleForServiceProvider(serviceProviderId, schedule);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "{/serviceProviderId}")
    public ResponseEntity<Void> updateScheduleForServiceProvider(
            @PathVariable long serviceProviderId,
            @Valid @NotNull @Size(min = 1) @RequestBody LinkedHashMap<LocalDate, List<AvailabilityIntervalDto>> schedule) {

        scheduleService.updateScheduleForServiceProvider(serviceProviderId, schedule);
        return ResponseEntity.noContent().build();
    }
}
