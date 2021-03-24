package com.github.vikusku.happytires.dto;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReservableIntervalDto {
    private LocalDateTime start;
    private Duration duration;
    private long serviceProviderId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservableIntervalDto that = (ReservableIntervalDto) o;
        return serviceProviderId == that.serviceProviderId &&
                Objects.equal(start, that.start) &&
                Objects.equal(duration, that.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(start, duration, serviceProviderId);
    }

    @Override
    public String toString() {
        return "ReservableIntervalDto{" +
                "start=" + start +
                ", duration=" + duration +
                ", serviceProviderId=" + serviceProviderId +
                '}';
    }
}
