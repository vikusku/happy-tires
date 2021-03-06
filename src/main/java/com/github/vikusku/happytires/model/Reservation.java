package com.github.vikusku.happytires.model;

import com.google.common.base.Objects;
import lombok.*;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private long id;
    private LocalDateTime start;
    private Duration duration;
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;
    @ManyToOne(optional = false)
    @JoinColumn(name = "customerId", referencedColumnName = "id")
    private Customer customer;
    @OneToMany(mappedBy = "reservation", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.DETACH})
    private List<TimeSlot> timeSlots;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return id == that.id &&
                Objects.equal(start, that.start) &&
                Objects.equal(duration, that.duration) &&
                serviceType == that.serviceType &&
                Objects.equal(customer, that.customer);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, start, duration, serviceType, customer);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", start=" + start +
                ", duration=" + duration +
                ", serviceType=" + serviceType +
                ", customer=" + customer +
                '}';
    }
}
