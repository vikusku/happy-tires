package com.github.vikusku.happytires.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceProviderDto {
    private Long id;
    @NotEmpty(message = "Name cannot be null")
    private String name;
    @Email(message = "Email should be valid")
    private String email;
    @NotEmpty(message = "Phone number cannot be null")
    private String phoneNumber;
}
