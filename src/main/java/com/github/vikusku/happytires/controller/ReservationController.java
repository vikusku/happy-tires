package com.github.vikusku.happytires.controller;

import com.github.vikusku.happytires.dto.ReservationDto;
import com.github.vikusku.happytires.model.Reservation;
import com.github.vikusku.happytires.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

@RestController
@RequestMapping(path = "/api/v1/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto> get(@PathVariable long id) {
        return reservationService.get(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ReservationDto> create(@Valid @NotNull @RequestBody final ReservationDto reservationDto) {
        return reservationService.create(reservationDto).map(persisted -> {
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(persisted.getId())
                    .toUri();

            return ResponseEntity.created(uri).body(persisted);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ReservationDto> update(
            @PathVariable  long id, @Valid @NotNull @RequestBody final ReservationDto reservationDto) {

        return reservationService.update(id, reservationDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final long id) {
        if (reservationService.delete(id).isPresent()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
