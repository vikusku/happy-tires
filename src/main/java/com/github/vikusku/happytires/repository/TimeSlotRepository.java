package com.github.vikusku.happytires.repository;

import com.github.vikusku.happytires.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    @Query("select t from TimeSlot t where t.start >= ?1 and t.end <= ?2 and t.reservation is null")
    List<TimeSlot> findAvailableTimeSlots(LocalDateTime from, LocalDateTime until);
}
