package com.github.vikusku.happytires.service;

import com.github.vikusku.happytires.dto.request.ScheduleTimeSlotRequest;
import com.github.vikusku.happytires.dto.TimeSlotStatus;
import com.github.vikusku.happytires.exception.ServiceProviderNotFoundException;
import com.github.vikusku.happytires.exception.advice.InvalidUpdateDayAvailabilityRequest;
import com.github.vikusku.happytires.model.Reservation;
import com.github.vikusku.happytires.model.ServiceProvider;
import com.github.vikusku.happytires.model.TimeSlot;
import com.github.vikusku.happytires.repository.ServiceProviderRepository;
import com.github.vikusku.happytires.repository.TimeSlotRepository;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@WebMvcTest(ScheduleServiceTest.class)
class ScheduleServiceTest {

    @MockBean
    private TimeSlotRepository timeSlotRepository;

    @MockBean
    private ServiceProviderRepository spRepository;

    @MockBean
    private ModelMapper modelMapper;

    @Captor
    ArgumentCaptor<ServiceProvider> serviceProviderArgumentCaptor;

    private ScheduleService scheduleService;

    final Duration duration = Duration.ofMinutes(15);
    final long serviceProviderId = 1L;
    final ServiceProvider sp = new ServiceProvider(
            serviceProviderId, "Foo Bar", "foo.bar@test.com", "12341231234", Lists.newArrayList()
    );

    @BeforeEach
    void setUp() {
        scheduleService = new ScheduleService(timeSlotRepository, spRepository,modelMapper);
    }

    @AfterEach
    void tearDown() {
        clearInvocations(timeSlotRepository, spRepository, modelMapper);
    }

    @Test
    public void addTimeSlotsForServiceProviderShouldPersistTimeSlots() {
        LinkedHashMap<LocalDate, List<ScheduleTimeSlotRequest>> schedule = new LinkedHashMap<>();
        schedule.put(LocalDate.parse("2021-03-01"),
                Lists.newArrayList(
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.AVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.AVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("08:30:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.UNAVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("08:45:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.UNAVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("09:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.UNAVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("09:15:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.UNAVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("09:30:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.UNAVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("09:45:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.UNAVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("10:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.AVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("10:15:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.UNAVAILABLE)
                ));

        schedule.put(LocalDate.parse("2021-03-02"),
                Lists.newArrayList(
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("11:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.AVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("11:15:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.AVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("11:30:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.AVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("11:45:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.UNAVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("16:00:00+02:00",DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.AVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("16:15:00+02:00",DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.UNAVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("16:30:00+02:00",DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.UNAVAILABLE)
                ));

        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));

        scheduleService.addTimeSlotsForServiceProvider(serviceProviderId, schedule);

        verify(spRepository, times(1)).save(serviceProviderArgumentCaptor.capture());

        final List<TimeSlot> timeSlots = serviceProviderArgumentCaptor.getValue().getTimeSlots();

        assertThat(timeSlots).hasSize(7);
        assertThat(timeSlots).containsExactly(
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null),
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null),
                new TimeSlot(LocalDateTime.parse("2021-03-01T10:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null),
                new TimeSlot(LocalDateTime.parse("2021-03-02T11:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null),
                new TimeSlot(LocalDateTime.parse("2021-03-02T11:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null),
                new TimeSlot(LocalDateTime.parse("2021-03-02T11:30:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null),
                new TimeSlot(LocalDateTime.parse("2021-03-02T16:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null)
        );
    }

    @Test
    public void addTimeSlotsForServiceProviderShouldException() {
        LinkedHashMap<LocalDate, List<ScheduleTimeSlotRequest>> schedule = new LinkedHashMap<>();

        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ServiceProviderNotFoundException.class, () -> {
            scheduleService.addTimeSlotsForServiceProvider(serviceProviderId, schedule);
        });

        String expectedMessage = "Service provider with id=[1] does not exist";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void updateTimeSlotsForServiceProviderShouldAddNewSlots() {
        final LinkedHashMap<LocalDate, List<ScheduleTimeSlotRequest>> schedule = new LinkedHashMap<>();
        schedule.put(LocalDate.parse("2021-03-01"),
                Lists.newArrayList(
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.AVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.AVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("08:30:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.AVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("08:45:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.AVAILABLE)
                ));

        sp.setTimeSlots(Lists.newArrayList(
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null),
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null)));

        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));

        scheduleService.updateTimeSlotsForServiceProvider(serviceProviderId, schedule);
        verify(spRepository, times(1)).save(serviceProviderArgumentCaptor.capture());

        final List<TimeSlot> updatedTimeSlots = serviceProviderArgumentCaptor.getValue().getTimeSlots();

        assertThat(updatedTimeSlots).hasSize(4);
        assertThat(updatedTimeSlots).containsExactly(
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null),
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null),
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:30:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null),
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:45:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null)
        );
    }

    @Test
    public void updateTimeSlotsForServiceProviderShouldRemoveOldSlots() {
        final LinkedHashMap<LocalDate, List<ScheduleTimeSlotRequest>> schedule = new LinkedHashMap<>();
        schedule.put(LocalDate.parse("2021-03-01"),
                Lists.newArrayList(
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.AVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.AVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("08:30:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.UNAVAILABLE),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("08:45:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.UNAVAILABLE)
                ));

        sp.setTimeSlots(Lists.newArrayList(
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null),
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null),
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:30:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null),
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:45:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null)));

        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));

        scheduleService.updateTimeSlotsForServiceProvider(serviceProviderId, schedule);
        verify(spRepository, times(1)).save(serviceProviderArgumentCaptor.capture());

        final List<TimeSlot> updatedTimeSlots = serviceProviderArgumentCaptor.getValue().getTimeSlots();

        assertThat(updatedTimeSlots).hasSize(2);
        assertThat(updatedTimeSlots).containsExactly(
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null),
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null));
    }

    @Test
    public void updateTimeSlotsForServiceProviderShouldNotChangeSlotsWithReservation() {
        final LinkedHashMap<LocalDate, List<ScheduleTimeSlotRequest>> schedule = new LinkedHashMap<>();
        schedule.put(LocalDate.parse("2021-03-01"),
                Lists.newArrayList(
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.RESERVED),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.RESERVED)
                ));

        sp.setTimeSlots(Lists.newArrayList(
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, new Reservation()),
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, new Reservation())));

        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));

        scheduleService.updateTimeSlotsForServiceProvider(serviceProviderId, schedule);
        verify(spRepository, times(1)).save(serviceProviderArgumentCaptor.capture());

        final List<TimeSlot> updatedTimeSlots = serviceProviderArgumentCaptor.getValue().getTimeSlots();
        assertThat(updatedTimeSlots).hasSize(2);

        assertNotNull(updatedTimeSlots.get(0).getReservation());
        assertNotNull(updatedTimeSlots.get(1).getReservation());
    }

    @Test
    public void updateTimeSlotsForServiceProviderShouldNotAllowRemovingReservedSlots() {
        final LinkedHashMap<LocalDate, List<ScheduleTimeSlotRequest>> schedule = new LinkedHashMap<>();
        schedule.put(LocalDate.parse("2021-03-01"),
                Lists.newArrayList(
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.RESERVED),
                        new ScheduleTimeSlotRequest(
                                LocalTime.parse("08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME), TimeSlotStatus.UNAVAILABLE)
                ));

        sp.setTimeSlots(Lists.newArrayList(
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, new Reservation()),
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, new Reservation())));

        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));

        Exception exception = assertThrows(InvalidUpdateDayAvailabilityRequest.class, () -> {
            scheduleService.updateTimeSlotsForServiceProvider(serviceProviderId, schedule);
        });

        String expectedMessage = "Removing time slots with reservation is not allowed";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}