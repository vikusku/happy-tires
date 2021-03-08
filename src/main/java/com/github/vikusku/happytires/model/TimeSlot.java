package com.github.vikusku.happytires.model;

import com.google.common.base.Objects;
import lombok.*;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table
@IdClass(TimeSlotPK.class)
public class TimeSlot {
    @Id
    private LocalDateTime start;
    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "serviceProviderId", referencedColumnName = "id")
    private ServiceProvider serviceProvider;
    private Duration duration;
    @ManyToOne(optional = false)
    @JoinColumn(name = "reservationId", referencedColumnName = "id")
    private Reservation reservation;

    @Override
    public String toString() {
        return "TimeSlot [start=" + start +
                ", serviceProvider=" + serviceProvider.toString() +
                ", duration=" + duration +
                ", reservation=" + Optional.ofNullable(reservation).map(Reservation::toString).orElse("null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equal(start, timeSlot.start) &&
                Objects.equal(serviceProvider, timeSlot.serviceProvider) &&
                Objects.equal(duration, timeSlot.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(start, serviceProvider, duration);
    }
}
