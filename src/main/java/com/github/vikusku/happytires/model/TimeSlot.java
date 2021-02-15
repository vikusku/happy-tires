package com.github.vikusku.happytires.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    @ManyToOne(optional = false)
    @JoinColumn(name = "reservationId", referencedColumnName = "id")
    private Reservation reservation;
    @ManyToOne(optional = false)
    @JoinColumn(name = "serviceProviderId", referencedColumnName = "id")
    private ServiceProvider serviceProvider;
}
