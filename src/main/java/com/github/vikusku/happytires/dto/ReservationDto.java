package com.github.vikusku.happytires.dto;

import com.github.vikusku.happytires.model.Reservation;
import com.github.vikusku.happytires.model.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDto {
    private long id;
    private LocalDateTime start;
    private Duration duration;
    private ServiceType serviceType;
    private String customerName;
    private String customerAddress;
    private String customerEmail;
    private String customerPhoneNumber;
}
