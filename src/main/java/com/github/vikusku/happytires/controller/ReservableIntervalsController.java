package com.github.vikusku.happytires.controller;

import com.github.vikusku.happytires.dto.ReservableIntervalDto;
import com.github.vikusku.happytires.model.ServiceType;
import com.github.vikusku.happytires.service.ReservableIntervalsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/reservable-intervals")
public class ReservableIntervalsController {

    @Autowired
    private ReservableIntervalsService reservableIntervalsService;

    @GetMapping
    public ResponseEntity<Map<LocalDate, List<ReservableIntervalDto>>> findReservableIntervals(
            @RequestParam @NotNull long serviceProviderId,
            @RequestParam @NotNull ServiceType serviceType,
            @RequestParam @NotNull @FutureOrPresent LocalDate from,
            @RequestParam @NotNull @FutureOrPresent LocalDate until) {

        return ResponseEntity.ok(reservableIntervalsService.findReservableIntervals(serviceProviderId, serviceType, from, until));
    }
}
