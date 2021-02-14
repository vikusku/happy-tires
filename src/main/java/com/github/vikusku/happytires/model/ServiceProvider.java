package com.github.vikusku.happytires.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceProvider {
    private long id;
    private String name;
    private String email;
    private String phoneNumber;
}
