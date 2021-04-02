package com.github.vikusku.happytires.service;

import com.github.vikusku.happytires.dto.CustomerDto;
import com.github.vikusku.happytires.dto.ReservationDto;
import com.github.vikusku.happytires.exception.ReservationPersistenceException;
import com.github.vikusku.happytires.model.Customer;
import com.github.vikusku.happytires.model.Reservation;
import com.github.vikusku.happytires.model.TimeSlot;
import com.github.vikusku.happytires.model.TimeSlotPK;
import com.github.vikusku.happytires.repository.ReservationRepository;
import com.github.vikusku.happytires.repository.TimeSlotRepository;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.github.vikusku.happytires.util.Constants.DEFAULT_TIME_SLOT_DURATION;

// TODO add tests
@Service
@AllArgsConstructor
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    public Optional<ReservationDto> get(final long id) {
        return reservationRepository.findById(id).map(this::fromReservation);
    }

    // TODO add transaction
    public Optional<ReservationDto> create(final ReservationDto reservationDto) {
        final Customer customer = new Customer();
        customer.setName(reservationDto.getCustomerDto().getName());
        customer.setEmail(reservationDto.getCustomerDto().getEmail());
        customer.setAddress(reservationDto.getCustomerDto().getAddress());
        customer.setPhoneNumber(reservationDto.getCustomerDto().getPhoneNumber());

        final Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setStart(reservationDto.getStart());
        reservation.setDuration(reservationDto.getDuration());
        reservation.setServiceType(reservationDto.getServiceType());

        List<TimeSlot> timeSlots = updateTimeSlots(reservation, reservationDto.getServiceProviderId());
        reservation.setTimeSlots(timeSlots);

        return Optional.ofNullable(fromReservation(reservationRepository.save(reservation)));
    }

    private List<TimeSlot> updateTimeSlots(final Reservation reservation, final long serviceProviderId) {
        final List<TimeSlot> slots = Lists.newArrayList();
        final LocalDateTime reservationStart = reservation.getStart();
        final LocalDateTime reservationEnd = reservationStart.plusMinutes(reservation.getDuration().toMinutes());

        for(LocalDateTime slotStart = reservationStart;
            !slotStart.isEqual(reservationEnd);
            slotStart = slotStart.plusMinutes(DEFAULT_TIME_SLOT_DURATION.toMinutes())) {

            final Optional<TimeSlot> tsOpt = timeSlotRepository.findById(new TimeSlotPK(slotStart, serviceProviderId));

            if (tsOpt.isPresent()) {
                final TimeSlot ts = tsOpt.get();
                if (ts.getReservation() != null && ts.getReservation().getId() != reservation.getId()) {
                    throw new ReservationPersistenceException("TIME_SLOTS_ALREADY_RESERVED");
                }

                ts.setReservation(reservation);
                slots.add(ts);
            } else {
                throw new ReservationPersistenceException("NO_AVAILABLE_TIME_SLOTS");
            }
        }

        timeSlotRepository.saveAll(slots);

        return slots;
    }

    public Optional<ReservationDto> update(final long id, final ReservationDto reservationDto) {
        return reservationRepository.findById(id)
                .map(existingReservation -> {

                    existingReservation.setStart(reservationDto.getStart());
                    existingReservation.setDuration(reservationDto.getDuration());
                    existingReservation.setServiceType(reservationDto.getServiceType());

                    List<TimeSlot> timeSlots = updateTimeSlots(existingReservation, reservationDto.getServiceProviderId());
                    existingReservation.setTimeSlots(timeSlots);

                    return Optional.ofNullable(fromReservation(reservationRepository.save(existingReservation)));
                })
                .orElseGet(Optional::empty);
    }

    public void delete(final long id) {
        reservationRepository.deleteById(id);
    }

    public ReservationDto fromReservation(final Reservation reservation) {
        final ReservationDto reservationDto = new ReservationDto();
        reservationDto.setId(reservation.getId());
        reservationDto.setStart(reservation.getStart());
        reservationDto.setDuration(reservation.getDuration());
        reservationDto.setServiceType(reservation.getServiceType());
        reservationDto.setServiceProviderId(reservation.getTimeSlots().get(0).getServiceProvider().getId());
        reservationDto.setCustomerDto(new CustomerDto(
                reservation.getCustomer().getName(),
                reservation.getCustomer().getAddress(),
                reservation.getCustomer().getEmail(),
                reservation.getCustomer().getPhoneNumber()
        ));

        return reservationDto;
    }
}
