package com.github.vikusku.happytires.controller;

import com.github.vikusku.happytires.dto.ReservationDto;
import com.github.vikusku.happytires.model.Reservation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/reservations")
public class ReservationController {

    public ResponseEntity<Reservation> getReservation(final long id) {
        return null;
    }

    public ResponseEntity<Void> createReservation(final ReservationDto reservationDto) {

        return null;
    }

    public ResponseEntity<Void> updateReservation(final ReservationDto reservationDto) {

        return null;
    }

    public ResponseEntity<Void> cancelReservation(final long id) {

        return null;
    }
}
