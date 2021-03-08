package com.github.vikusku.happytires.repository;

import com.github.vikusku.happytires.model.TimeSlot;
import com.github.vikusku.happytires.model.TimeSlotPK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, TimeSlotPK> {

}
