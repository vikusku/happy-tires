package com.github.vikusku.happytires.dto;

import com.github.vikusku.happytires.model.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDto {
    private ServiceType serviceType;
    private String customerName;
    private String customerAddress;
    private String customerEmail;
    private String customerPhoneNumber;
}
