package com.github.vikusku.happytires.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private long id;
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;
    @ManyToOne(optional = false)
    @JoinColumn(name = "customerId", referencedColumnName = "id")
    private Customer customer;
    @OneToMany(mappedBy = "reservation", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.DETACH})
    private List<TimeSlot> timeSlots;
}
