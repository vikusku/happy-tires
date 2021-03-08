package com.github.vikusku.happytires.service;

import com.github.vikusku.happytires.dto.ReservationDto;
import com.github.vikusku.happytires.dto.request.ScheduleTimeSlotRequest;
import com.github.vikusku.happytires.dto.TimeSlotStatus;
import com.github.vikusku.happytires.dto.response.TimeSlotAvailabilityResponse;
import com.github.vikusku.happytires.exception.ServiceProviderNotFoundException;
import com.github.vikusku.happytires.exception.advice.InvalidUpdateDayAvailabilityRequest;
import com.github.vikusku.happytires.model.ServiceProvider;
import com.github.vikusku.happytires.model.TimeSlot;
import com.github.vikusku.happytires.model.TimeSlotPK;
import com.github.vikusku.happytires.repository.ServiceProviderRepository;
import com.github.vikusku.happytires.repository.TimeSlotRepository;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ScheduleService {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private ServiceProviderRepository spRepository;

    @Autowired
    private ModelMapper modelMapper;

    private final static Duration DEFAULT_TIME_SLOT_DURATION = Duration.ofMinutes(15);

    public List<TimeSlotAvailabilityResponse> findAllAvailableTimeSlots(final LocalDateTime start, final LocalDateTime end) {
//        return modelMapperUtil.mapList(timeSlotRepository.findAvailableTimeSlots(start), TimeSlotDto.class);
         return Lists.newArrayList();
    }

    public Map<LocalDate, TimeSlotAvailabilityResponse> findTimeSlotsForServiceProvider(
            final long serviceProviderId,
            final LocalDateTime from,
            final LocalDateTime until
    ) {
        return  spRepository.findById(serviceProviderId).map(sp -> {
            Map<LocalDate, TimeSlotAvailabilityResponse> r = new HashMap<>();
            return r;
        }).orElseThrow(
                () -> new ServiceProviderNotFoundException(String.format("Service provider with %d does not exist", serviceProviderId)));
    }

    public void addTimeSlotsForServiceProvider(final long serviceProviderId,
                                               final LinkedHashMap<LocalDate, List<ScheduleTimeSlotRequest>> schedule) {
        final Optional<ServiceProvider> spOpt = spRepository.findById(serviceProviderId);
        if (spOpt.isPresent()) {
            final ServiceProvider sp = spOpt.get();

            List<TimeSlot> timeSlots = schedule.entrySet().stream()
                    .map(dayAvailability -> createTimeSlotsForDay(dayAvailability.getKey(), dayAvailability.getValue(), sp))
                    .flatMap(List::stream).collect(Collectors.toList());

            sp.setTimeSlots(timeSlots);
            spRepository.save(sp);
        } else {
            throw new ServiceProviderNotFoundException(
                    String.format("Service provider with id=[%d] does not exist", serviceProviderId)
            );
        }
    }

    public void updateTimeSlotsForServiceProvider(final long serviceProviderId,
                                                  final LinkedHashMap<LocalDate, List<ScheduleTimeSlotRequest>> schedule) {
        final Optional<ServiceProvider> spOpt = spRepository.findById(serviceProviderId);
        if (spOpt.isPresent()) {
            final ServiceProvider sp = spOpt.get();

            List<TimeSlot> updatedTimeSlots = schedule.entrySet().stream()
                    .map(dayAvailability -> createTimeSlotsForDay(dayAvailability.getKey(), dayAvailability.getValue(), sp))
                    .flatMap(List::stream).collect(Collectors.toList());

            List<TimeSlot> merged = merge(updatedTimeSlots, sp.getTimeSlots());

            sp.setTimeSlots(merged);
            spRepository.save(sp);
        } else {
            throw new ServiceProviderNotFoundException(
                    String.format("Service provider with id=[%d] does not exist", serviceProviderId)
            );
        }
    }

    private List<TimeSlot> createTimeSlotsForDay(
            final LocalDate date,
            final List<ScheduleTimeSlotRequest> daySchedule,
            final ServiceProvider sp) {

        return daySchedule.stream()
                .filter(timeSlot -> TimeSlotStatus.AVAILABLE.equals(timeSlot.getStatus()))
                .map(timeSlot -> {
                    final TimeSlot persistableTs = new TimeSlot();
                    persistableTs.setStart(LocalDateTime.of(date, timeSlot.getStart()));
                    persistableTs.setServiceProvider(sp);
                    persistableTs.setDuration(DEFAULT_TIME_SLOT_DURATION);

                    return persistableTs;
        }).collect(Collectors.toList());
    }

    private List<TimeSlot> merge(List<TimeSlot> updatedTimeSlots, List<TimeSlot> currentTimeSlots) {
        final List<TimeSlot> merged = new ArrayList<>(currentTimeSlots);

        currentTimeSlots.forEach(cTs -> {
            if (updatedTimeSlots.contains(cTs)) {
                updatedTimeSlots.remove(cTs);
            } else {
                if (cTs.getReservation() == null) {
                    merged.remove(cTs);
                } else {
                    throw new InvalidUpdateDayAvailabilityRequest("Removing time slots with reservation is not allowed");
                }
            }
        });

        merged.addAll(updatedTimeSlots);

        return merged;
    }

    private List<TimeSlotAvailabilityResponse> generatedGrid(LocalDate date, Long serviceProviderId) {
            List<TimeSlotAvailabilityResponse> dayGrid = new ArrayList<>();
            LocalTime slotStart = LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME);
            LocalTime endOfDay = LocalTime.parse("22:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME);


        for ( ;slotStart.isBefore(endOfDay); slotStart = slotStart.plusMinutes(DEFAULT_TIME_SLOT_DURATION.toMinutes())) {

            final TimeSlotAvailabilityResponse timeSlotAvailability = new TimeSlotAvailabilityResponse();
            timeSlotAvailability.setStart(slotStart);
//            timeSlotAvailability.setDuration(DEFAULT_TIME_SLOT_DURATION);
            timeSlotAvailability.setStatus(TimeSlotAvailabilityResponse.Status.UNAVAILABLE);

            final Optional<TimeSlot> tsOpt = timeSlotRepository.findById(
                    new TimeSlotPK(LocalDateTime.of(date, slotStart), serviceProviderId));

            if (tsOpt.isPresent()) {
                if (tsOpt.get().getReservation() != null) {
                    timeSlotAvailability.setReservationDto(modelMapper.map(tsOpt.get().getReservation(), ReservationDto.class));
                    timeSlotAvailability.setStatus(TimeSlotAvailabilityResponse.Status.RESERVED);
                } else {
                    timeSlotAvailability.setStatus(TimeSlotAvailabilityResponse.Status.AVAILABLE);
                }
            }

            dayGrid.add(timeSlotAvailability);
        }

        return dayGrid;
   }
}
