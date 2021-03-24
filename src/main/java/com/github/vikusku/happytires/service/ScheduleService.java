package com.github.vikusku.happytires.service;

import com.github.vikusku.happytires.dto.AvailabilityIntervalDto;
import com.github.vikusku.happytires.dto.IntervalStatus;
import com.github.vikusku.happytires.dto.ReservationDto;
import com.github.vikusku.happytires.dto.ScheduleIntervalDto;
import com.github.vikusku.happytires.exception.InvalidScheduleException;
import com.github.vikusku.happytires.exception.ServiceProviderNotFoundException;
import com.github.vikusku.happytires.model.ServiceProvider;
import com.github.vikusku.happytires.model.TimeSlot;
import com.github.vikusku.happytires.repository.ServiceProviderRepository;
import com.github.vikusku.happytires.repository.TimeSlotRepository;
import com.github.vikusku.happytires.util.Constants;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.vikusku.happytires.util.Constants.DEFAULT_TIME_SLOT_DURATION;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.stream.Collectors.groupingBy;

// TODO Create parent interface for Reservation and Interval
@AllArgsConstructor
@Service
public class ScheduleService {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private ServiceProviderRepository spRepository;

    private final static Duration FULL_DAY = Duration.ofMinutes(13 * 60);
    private final static LocalTime START_OF_DAY = LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME);
    private final static LocalTime END_OF_DAY = LocalTime.parse("21:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME);

    public Map<LocalDate, List<ScheduleIntervalDto>> getScheduleForServiceProvider(
            final long serviceProviderId,
            final LocalDate from,
            final LocalDate until) {
        return  spRepository.findById(serviceProviderId)
                .map(sp -> {
                    final List<TimeSlot> timeSlots = timeSlotRepository.findByServiceProviderAndStartAfterAndStartBefore(
                            sp, LocalDateTime.of(from, LocalTime.MIN), LocalDateTime.of(until, LocalTime.MAX));

                    Map<LocalDate, List<ScheduleIntervalDto>> intervalsFromTimeSlots = timeSlots.stream()
                            .collect(groupingBy(ts -> ts.getStart().toLocalDate()))
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    e ->  parseTimeSlots(e.getValue())
                            ));

                    return generateGrid(from, until, intervalsFromTimeSlots);
                })
                .orElseThrow(() -> new ServiceProviderNotFoundException(String.format("Service provider with %d does not exist", serviceProviderId)));
    }
    private Map<LocalDate, List<ScheduleIntervalDto>> generateGrid(LocalDate from, LocalDate until, Map<LocalDate,
            List<ScheduleIntervalDto>> intervalsFromTimeSlots) {
        Map<LocalDate, List<ScheduleIntervalDto>> schedule = Maps.newLinkedHashMap();

        for (LocalDate date = from; date.isBefore(until); date = date.plusDays(1)) {
            final List<ScheduleIntervalDto> daySchedule = Lists.newArrayList();

            final List<ScheduleIntervalDto> dayIntervalsFromTimeSlots = intervalsFromTimeSlots.get(date);
            if (dayIntervalsFromTimeSlots == null) {
                daySchedule.add(new ScheduleIntervalDto(START_OF_DAY, FULL_DAY, null, IntervalStatus.UNAVAILABLE));
            } else {
                ScheduleIntervalDto previousInterval = dayIntervalsFromTimeSlots.get(0);
                if (START_OF_DAY.isBefore(previousInterval.getStart())) {
                    daySchedule.add(new ScheduleIntervalDto(START_OF_DAY,
                            Duration.ofMinutes(START_OF_DAY.until(previousInterval.getStart(), MINUTES)), null, IntervalStatus.UNAVAILABLE));
                }

                for (int intervalIndex = 1; intervalIndex < dayIntervalsFromTimeSlots.size(); intervalIndex++) {

                    daySchedule.add(previousInterval);

                    final ScheduleIntervalDto currentInterval = dayIntervalsFromTimeSlots.get(intervalIndex);
                    final LocalTime endOfPreviousInterval = previousInterval.getStart().plusMinutes(previousInterval.getDurationMin().toMinutes());

                    if (endOfPreviousInterval.isBefore(currentInterval.getStart())) {
                        daySchedule.add(new ScheduleIntervalDto(endOfPreviousInterval,
                                Duration.ofMinutes(endOfPreviousInterval.until(currentInterval.getStart(), MINUTES)),
                                null, IntervalStatus.UNAVAILABLE));
                    }
                    previousInterval = currentInterval;
                }

                daySchedule.add(previousInterval);
                final LocalTime endOfLastInterval = previousInterval.getStart().plusMinutes(previousInterval.getDurationMin().toMinutes());

                if (endOfLastInterval.isBefore(END_OF_DAY)) {
                    daySchedule.add(new ScheduleIntervalDto(endOfLastInterval,
                            Duration.ofMinutes(endOfLastInterval.until(END_OF_DAY, MINUTES)), null, IntervalStatus.UNAVAILABLE));
                }
            }

            schedule.put(date, daySchedule);
        }

        return schedule;
    }

    private List<ScheduleIntervalDto> parseTimeSlots(List<TimeSlot> timeSlots) {
        List<ScheduleIntervalDto> dayGrid = new ArrayList<>();


        final TimeSlot firstSlot = timeSlots.get(0);
        ScheduleIntervalDto interval = createInterval(firstSlot);

        for (int slotIndex = 1; slotIndex < timeSlots.size(); slotIndex++) {
            final TimeSlot timeSlot = timeSlots.get(slotIndex);

            if (isSameInterval(timeSlot, interval)) {
                interval.setDurationMin(interval.getDurationMin().plusMinutes(timeSlot.getDuration().toMinutes()));
            } else {
                dayGrid.add(interval);
                interval = createInterval(timeSlot);
            }
        }

        dayGrid.add(interval);
        return dayGrid;
    }

    private boolean isSameInterval(final TimeSlot nextTimeSlot, final ScheduleIntervalDto currentInterval) {
        final LocalTime endOfCurrentInterval = currentInterval.getStart().plusMinutes(currentInterval.getDurationMin().toMinutes());
        if (endOfCurrentInterval.until(nextTimeSlot.getStart(), MINUTES) >= DEFAULT_TIME_SLOT_DURATION.toMinutes()) {
            return false;
        }

        if (currentInterval.getStatus().equals(IntervalStatus.AVAILABLE)) {
            return nextTimeSlot.getReservation() == null;
        }

        if (currentInterval.getStatus().equals(IntervalStatus.RESERVED)) {
            if (nextTimeSlot.getReservation() == null) {
                return false;
            } else {
                return currentInterval.getReservationDto().getId() == nextTimeSlot.getReservation().getId();
            }
        }

        return false;
    }

    private ScheduleIntervalDto createInterval(final TimeSlot timeSlot) {
        ScheduleIntervalDto interval = new ScheduleIntervalDto();
        interval.setStart(timeSlot.getStart().toLocalTime());
        interval.setDurationMin(timeSlot.getDuration());

        if (timeSlot.getReservation() == null) {
            interval.setStatus(IntervalStatus.AVAILABLE);
        } else {
            interval.setStatus(IntervalStatus.RESERVED);
            // TODO improve this. User Model mapper.
            interval.setReservationDto(new ReservationDto(
                    timeSlot.getReservation().getId(),
                    timeSlot.getReservation().getStart(),
                    timeSlot.getReservation().getDuration(),
                    timeSlot.getReservation().getServiceType(),
                    timeSlot.getReservation().getCustomer().getName(),
                    timeSlot.getReservation().getCustomer().getAddress(),
                    timeSlot.getReservation().getCustomer().getEmail(),
                    timeSlot.getReservation().getCustomer().getPhoneNumber()
            ));
        }

        return interval;
    }

    public void createScheduleForServiceProvider(final long serviceProviderId,
                                                 final LinkedHashMap<LocalDate, List<AvailabilityIntervalDto>> schedule) {
        final Optional<ServiceProvider> spOpt = spRepository.findById(serviceProviderId);
        if (spOpt.isPresent()) {
            final ServiceProvider sp = spOpt.get();

            List<TimeSlot> timeSlots = schedule.entrySet().stream()
                    .map(dayAvailability -> createAvailableTimeSlotsForDay(dayAvailability.getKey(), dayAvailability.getValue(), sp))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            sp.setTimeSlots(timeSlots);
            spRepository.save(sp);
        } else {
            throw new ServiceProviderNotFoundException(
                    String.format("Service provider with id=[%d] does not exist", serviceProviderId)
            );
        }
    }

    public void updateScheduleForServiceProvider(final long serviceProviderId,
                                                 final LinkedHashMap<LocalDate, List<AvailabilityIntervalDto>> schedule) {
        final Optional<ServiceProvider> spOpt = spRepository.findById(serviceProviderId);
        if (spOpt.isPresent()) {
            final ServiceProvider sp = spOpt.get();

            List<TimeSlot> updatedAvailableTimeSlots = schedule.entrySet().stream()
                    .map(dayAvailability -> createAvailableTimeSlotsForDay(dayAvailability.getKey(), dayAvailability.getValue(), sp))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            List<TimeSlot> merged = merge(updatedAvailableTimeSlots, sp.getTimeSlots());

            sp.setTimeSlots(merged);
            spRepository.save(sp);
        } else {
            throw new ServiceProviderNotFoundException(
                    String.format("Service provider with id=[%d] does not exist", serviceProviderId)
            );
        }
    }

    private List<TimeSlot> createAvailableTimeSlotsForDay(
            final LocalDate date,
            final List<AvailabilityIntervalDto> daySchedule,
            final ServiceProvider sp) {

        return daySchedule.stream()
                .map(interval -> {
                    final List<TimeSlot> timeSlots = Lists.newArrayList();

                    LocalDateTime slotStart = LocalDateTime.of(date, interval.getStart());
                    final LocalDateTime intervalEnd = calculateIntervalEnd(slotStart, interval.getDurationMin());

                    for ( ; slotStart.isBefore(intervalEnd); slotStart = slotStart.plusMinutes(DEFAULT_TIME_SLOT_DURATION.toMinutes())) {
                        final TimeSlot persistableTs = new TimeSlot();
                        persistableTs.setStart(slotStart);
                        persistableTs.setServiceProvider(sp);
                        persistableTs.setDuration(DEFAULT_TIME_SLOT_DURATION);

                        timeSlots.add(persistableTs);
                    }

                    return timeSlots;
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private LocalDateTime calculateIntervalEnd(final LocalDateTime slotStart, final Duration intervalDuration) {
        return slotStart.plusMinutes(DEFAULT_TIME_SLOT_DURATION.toMinutes() * intervalDuration.toMinutes() / DEFAULT_TIME_SLOT_DURATION.toMinutes());
    }

    private List<TimeSlot> merge(List<TimeSlot> updatedAvailableTimeSlots, List<TimeSlot> currentTimeSlots) {
        final List<TimeSlot> merged = new ArrayList<>(currentTimeSlots);

        currentTimeSlots.forEach(cTs -> {
            if (cTs.getReservation() == null) {
                if (updatedAvailableTimeSlots.contains(cTs)) {
                    updatedAvailableTimeSlots.remove(cTs);
                } else {
                    merged.remove(cTs);
                }
            } else {
                if (updatedAvailableTimeSlots.contains(cTs)) {
                    throw new InvalidScheduleException("Updating time slots with reservation is not allowed");
                }
            }
        });
        merged.addAll(updatedAvailableTimeSlots);

        return merged;
    }
}
