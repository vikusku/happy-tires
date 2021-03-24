package com.github.vikusku.happytires.service;

import com.github.vikusku.happytires.dto.ReservableIntervalDto;
import com.github.vikusku.happytires.exception.ServiceProviderNotFoundException;
import com.github.vikusku.happytires.model.ServiceProvider;
import com.github.vikusku.happytires.model.ServiceType;
import com.github.vikusku.happytires.model.TimeSlot;
import com.github.vikusku.happytires.repository.ServiceProviderRepository;
import com.github.vikusku.happytires.repository.TimeSlotRepository;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static com.github.vikusku.happytires.util.Constants.DEFAULT_TIME_SLOT_DURATION;
import static java.util.stream.Collectors.groupingBy;

@AllArgsConstructor
@Service
public class ReservableIntervalsService {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private ServiceProviderRepository spRepository;

    @Autowired
    private DurationService durationService;

    public Map<LocalDate, List<ReservableIntervalDto>> findReservableIntervals(
            long serviceProviderId, ServiceType serviceType, LocalDate from, LocalDate until) {

        return spRepository.findById(serviceProviderId)
            .map(sp -> {
                final Map<LocalDate, List<TimeSlot>> groupedTimeSlots = getGroupedTimeSlots(sp, from, until);
                int minimumAdjacentSlots = getMinimumAdjacentSlots(serviceType);

                final Map<LocalDate, List<ReservableIntervalDto>> availableIntervals = Maps.newLinkedHashMap();

                for (LocalDate date = from; date.isBefore(until); date = date.plusDays(1)) {
                    availableIntervals.put(date, createReservableIntervals(
                            minimumAdjacentSlots, groupedTimeSlots.getOrDefault(date, Lists.newArrayList()), sp));
                }


                return  availableIntervals;

            })
            .orElseThrow(() -> new ServiceProviderNotFoundException(String.format("Service provider with %d does not exist", serviceProviderId)));
    }

    private List<ReservableIntervalDto> createReservableIntervals(int minimumAdjacentSlots, List<TimeSlot> dateTimeSlots, ServiceProvider sp) {
        final List<ReservableIntervalDto> reservableIntervals = Lists.newArrayList();

            for (int intervalStartIndex = 0; intervalStartIndex <= dateTimeSlots.size() - minimumAdjacentSlots; intervalStartIndex++ ) {
                TimeSlot intervalStartSlot = dateTimeSlots.get(intervalStartIndex);
                TimeSlot currentSlot = intervalStartSlot;
                int adjacentSlotCount = 1;

                List<TimeSlot> timeSlotsSlice = dateTimeSlots.subList(intervalStartIndex + 1, intervalStartIndex + minimumAdjacentSlots);
                for (int nextSlotIndex = 0; nextSlotIndex < timeSlotsSlice.size(); nextSlotIndex++ ) {
                    TimeSlot nextSlot = timeSlotsSlice.get(nextSlotIndex);

                    if (currentSlot.getStart().plusMinutes(currentSlot.getDuration().toMinutes()).isEqual(nextSlot.getStart())) {
                        adjacentSlotCount++;
                    }

                    currentSlot = nextSlot;
                }

                if (adjacentSlotCount == minimumAdjacentSlots) {
                    reservableIntervals.add(new ReservableIntervalDto(
                            intervalStartSlot.getStart(),
                            Duration.ofMinutes(DEFAULT_TIME_SLOT_DURATION.toMinutes() * minimumAdjacentSlots),
                            sp.getId()
                    ));
                }
            }

        return reservableIntervals;
    }


    private Map<LocalDate, List<TimeSlot>> getGroupedTimeSlots(
            ServiceProvider sp, LocalDate from, LocalDate until) {
        List<TimeSlot> availableTimeSlots = timeSlotRepository.findByServiceProviderAndStartAfterAndStartBeforeAndReservationIsNull(
                sp, LocalDateTime.of(from, LocalTime.MIN), LocalDateTime.of(until, LocalTime.MAX));

        return availableTimeSlots.stream()
                .collect(groupingBy(ts -> ts.getStart().toLocalDate()));
    }

    private int getMinimumAdjacentSlots(ServiceType serviceType) {
        return (int) (durationService.getServiceDuration(serviceType) / DEFAULT_TIME_SLOT_DURATION.toMinutes());
    }

}
