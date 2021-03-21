package com.github.vikusku.happytires.service;

import com.github.vikusku.happytires.dto.AvailabilityIntervalDto;
import com.github.vikusku.happytires.exception.InvalidScheduleException;
import com.github.vikusku.happytires.exception.ServiceProviderNotFoundException;
import com.github.vikusku.happytires.model.*;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

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
        LinkedHashMap<LocalDate, List<AvailabilityIntervalDto>> schedule = new LinkedHashMap<>();
        schedule.put(LocalDate.parse("2021-03-01"),
                Lists.newArrayList(
                        new AvailabilityIntervalDto(
                                LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                                Duration.ofMinutes(30)),
                        new AvailabilityIntervalDto(
                                LocalTime.parse("10:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                                Duration.ofMinutes(15))
                ));

        schedule.put(LocalDate.parse("2021-03-02"),
                Lists.newArrayList(
                        new AvailabilityIntervalDto(
                                LocalTime.parse("11:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                                Duration.ofMinutes(45)),
                        new AvailabilityIntervalDto(
                                LocalTime.parse("16:00:00+02:00",DateTimeFormatter.ISO_OFFSET_TIME),
                                Duration.ofMinutes(15))
                ));

        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));

        scheduleService.createScheduleForServiceProvider(serviceProviderId, schedule);

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
    public void addTimeSlotsForServiceProviderThrowsServiceProviderNotFoundException() {
        LinkedHashMap<LocalDate, List<AvailabilityIntervalDto>> schedule = new LinkedHashMap<>();

        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ServiceProviderNotFoundException.class, () -> {
            scheduleService.createScheduleForServiceProvider(serviceProviderId, schedule);
        });

        String expectedMessage = "Service provider with id=[1] does not exist";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void updateTimeSlotsForServiceProviderShouldAddNewSlots() {
        final LinkedHashMap<LocalDate, List<AvailabilityIntervalDto>> schedule = new LinkedHashMap<>();
        schedule.put(LocalDate.parse("2021-03-01"),
                Lists.newArrayList(
                        new AvailabilityIntervalDto(
                                LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                                Duration.ofMinutes(60))
                ));

        sp.setTimeSlots(Lists.newArrayList(
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null),
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, null)));

        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));

        scheduleService.updateScheduleForServiceProvider(serviceProviderId, schedule);
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
        final LinkedHashMap<LocalDate, List<AvailabilityIntervalDto>> schedule = new LinkedHashMap<>();
        schedule.put(LocalDate.parse("2021-03-01"),
                Lists.newArrayList(
                        new AvailabilityIntervalDto(
                                LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                                Duration.ofMinutes(30))
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

        scheduleService.updateScheduleForServiceProvider(serviceProviderId, schedule);
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
        final LinkedHashMap<LocalDate, List<AvailabilityIntervalDto>> schedule = new LinkedHashMap<>();
        schedule.put(LocalDate.parse("2021-03-01"), Lists.newArrayList());

        sp.setTimeSlots(Lists.newArrayList(
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, new Reservation()),
                new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        sp, duration, new Reservation())));

        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));

        scheduleService.updateScheduleForServiceProvider(serviceProviderId, schedule);
        verify(spRepository, times(1)).save(serviceProviderArgumentCaptor.capture());

        final List<TimeSlot> updatedTimeSlots = serviceProviderArgumentCaptor.getValue().getTimeSlots();
        assertThat(updatedTimeSlots).hasSize(2);

        assertNotNull(updatedTimeSlots.get(0).getReservation());
        assertNotNull(updatedTimeSlots.get(1).getReservation());
    }

    @Test
    public void updateTimeSlotsForServiceProviderShouldNotAllowRemovingReservedSlots() {
        final LinkedHashMap<LocalDate, List<AvailabilityIntervalDto>> schedule = new LinkedHashMap<>();
        schedule.put(LocalDate.parse("2021-03-01"), Lists.newArrayList(
            new AvailabilityIntervalDto(
                    LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                    Duration.ofMinutes(30))
        ));

        final Customer customer = new Customer(1L, "foo", "foo street", "foo@email.com", "1231231234");
        final TimeSlot slot1 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot slot2 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final Reservation reservation = new Reservation(2L, ServiceType.TIRES_CHANGE, customer, Lists.newArrayList(slot1, slot2));
        slot1.setReservation(reservation);
        slot2.setReservation(reservation);

        sp.setTimeSlots(Lists.newArrayList(slot1, slot2));

        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));

        Exception exception = assertThrows(InvalidScheduleException.class, () -> {
            scheduleService.updateScheduleForServiceProvider(serviceProviderId, schedule);
        });

        String expectedMessage = "Updating time slots with reservation is not allowed";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}