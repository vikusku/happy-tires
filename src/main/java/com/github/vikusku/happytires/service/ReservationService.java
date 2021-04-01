package com.github.vikusku.happytires.service;

import com.github.vikusku.happytires.dto.ReservationDto;
import com.github.vikusku.happytires.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.Opt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    public Optional<ReservationDto> get(final long id) {
        return Optional.empty();
    }

    public Optional<ReservationDto> create(final ReservationDto reservationDto) {
        return Optional.empty();
    }

    public Optional<ReservationDto> update(final long id, final ReservationDto reservationDto) {
        return Optional.empty();
    }

    public Optional<ReservationDto> delete(final long id) {
        return Optional.empty();
    }
}
