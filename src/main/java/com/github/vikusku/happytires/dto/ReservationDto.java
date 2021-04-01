package com.github.vikusku.happytires.dto;

import com.github.vikusku.happytires.model.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDto {
    private long id;
    @NotEmpty(message = "Service provider id cannot be null")
    private long serviceProviderId;
    @FutureOrPresent
    private LocalDateTime start;
    private Duration duration;
    @NotNull
    private ServiceType serviceType;
    @NotNull
    private CustomerDto customerDto;
}
