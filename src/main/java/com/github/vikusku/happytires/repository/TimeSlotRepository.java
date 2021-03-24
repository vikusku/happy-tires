package com.github.vikusku.happytires.repository;

import com.github.vikusku.happytires.model.ServiceProvider;
import com.github.vikusku.happytires.model.TimeSlot;
import com.github.vikusku.happytires.model.TimeSlotPK;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, TimeSlotPK> {

    List<TimeSlot> findByServiceProviderAndStartAfterAndStartBefore(
            final ServiceProvider serviceProvider,
            final LocalDateTime after,
            final LocalDateTime before);

    List<TimeSlot> findByServiceProviderAndStartAfterAndStartBeforeAndReservationIsNull(
            final ServiceProvider serviceProvider,
            final LocalDateTime after,
            final LocalDateTime before);
}
