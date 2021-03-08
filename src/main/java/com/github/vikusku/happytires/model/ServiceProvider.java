package com.github.vikusku.happytires.model;

import com.google.common.base.Objects;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table
public class ServiceProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private long id;
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    private String phoneNumber;
    @OneToMany(mappedBy = "serviceProvider", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.DETACH})
    private List<TimeSlot> timeSlots;

    @Override
    public String toString() {
        return "ServiceProvider [id=" + id +
                ", name=" + name +
                ", email=" + email +
                ", phoneNumber=" + phoneNumber + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceProvider that = (ServiceProvider) o;
        return id == that.id &&
                Objects.equal(name, that.name) &&
                Objects.equal(email, that.email) &&
                Objects.equal(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, email, phoneNumber);
    }
}
