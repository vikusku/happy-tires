package com.github.vikusku.happytires.service;

import com.github.vikusku.happytires.dto.AvailabilityIntervalDto;
import com.github.vikusku.happytires.dto.IntervalStatus;
import com.github.vikusku.happytires.dto.ReservationDto;
import com.github.vikusku.happytires.dto.ScheduleIntervalDto;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.as;
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
        final Reservation reservation = new Reservation(2L, slot1.getStart(), Duration.ofMinutes(30), ServiceType.TIRES_CHANGE, customer, Lists.newArrayList(slot1, slot2));
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

    @Test
    public void getScheduleForServiceProviderGroupsAvailableOnlySlots() {
        // DAY 1 01-03-2021
        // AVAILABLE INTERVAL 08:00 - 09:00
        final TimeSlot ts1 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts2 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts3 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:30:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts4 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:45:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);

        // AVAILABLE INTERVAL 11:00 - 11:30
        final TimeSlot ts5 = new TimeSlot(LocalDateTime.parse("2021-03-01T11:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts6 = new TimeSlot(LocalDateTime.parse("2021-03-01T11:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);

        // AVAILABLE INTERVAL 16:00 - 16:30
        final TimeSlot ts7 = new TimeSlot(LocalDateTime.parse("2021-03-01T16:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts8 = new TimeSlot(LocalDateTime.parse("2021-03-01T16:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);

        List<TimeSlot> timeSlots = Lists.newArrayList(ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8);
        sp.setTimeSlots(timeSlots);
        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));
        when(timeSlotRepository.findByServiceProviderAndStartAfterAndStartBefore(
                any(ServiceProvider.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(timeSlots);

        final Map<LocalDate, List<ScheduleIntervalDto>> actualSchedule = scheduleService.getScheduleForServiceProvider(
                serviceProviderId,
                LocalDate.parse("2021-03-01"),
                LocalDate.parse("2021-03-02"));
        assertThat(actualSchedule).containsKeys(LocalDate.parse("2021-03-01"));
        assertThat(actualSchedule.get(LocalDate.parse("2021-03-01"))).containsExactly(
                new ScheduleIntervalDto(LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(60), null, IntervalStatus.AVAILABLE),
                new ScheduleIntervalDto(LocalTime.parse("09:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(2 * 60), null, IntervalStatus.UNAVAILABLE),
                new ScheduleIntervalDto(LocalTime.parse("11:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(30), null, IntervalStatus.AVAILABLE),
                new ScheduleIntervalDto(LocalTime.parse("11:30:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(4 * 60 + 30), null, IntervalStatus.UNAVAILABLE),
                new ScheduleIntervalDto(LocalTime.parse("16:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(30), null, IntervalStatus.AVAILABLE),
                new ScheduleIntervalDto(LocalTime.parse("16:30:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(4 * 60 + 30), null, IntervalStatus.UNAVAILABLE)
        );
    }

    @Test
    public void getScheduleForServiceProviderGroupsReservedOnlySlots() {
        final Customer customer = new Customer(10L, "Foo Bar", "foo street", "foo@test.com", "12312341231");

        // R1 09:00 - 09:30
        final TimeSlot ts1 = new TimeSlot(LocalDateTime.parse("2021-03-01T09:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts2 = new TimeSlot(LocalDateTime.parse("2021-03-01T09:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final Reservation r1 = new Reservation(100L, ts1.getStart(), Duration.ofMinutes(30), ServiceType.TIRES_CHANGE,
                customer, Lists.newArrayList(ts1, ts2));
        ts1.setReservation(r1);
        ts2.setReservation(r1);

        // R2 12:00 - 12:30
        final TimeSlot ts3 = new TimeSlot(LocalDateTime.parse("2021-03-01T12:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts4 = new TimeSlot(LocalDateTime.parse("2021-03-01T12:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final Reservation r2 = new Reservation(101L, ts3.getStart(), Duration.ofMinutes(30), ServiceType.TIRES_CHANGE,
                customer, Lists.newArrayList(ts3, ts4));
        ts3.setReservation(r2);
        ts4.setReservation(r2);

        // R3 12:30 - 13:15
        final TimeSlot ts5 = new TimeSlot(LocalDateTime.parse("2021-03-01T12:30:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts6 = new TimeSlot(LocalDateTime.parse("2021-03-01T12:45:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts7 = new TimeSlot(LocalDateTime.parse("2021-03-01T13:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final Reservation r3 = new Reservation(102L, ts5.getStart(), Duration.ofMinutes(45), ServiceType.TIRES_CHANGE,
                customer, Lists.newArrayList(ts5, ts6, ts7));
        ts5.setReservation(r3);
        ts6.setReservation(r3);
        ts7.setReservation(r3);

        List<TimeSlot> timeSlots = Lists.newArrayList(ts1, ts2, ts3, ts4, ts5, ts6, ts7);
        sp.setTimeSlots(timeSlots);
        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));
        when(timeSlotRepository.findByServiceProviderAndStartAfterAndStartBefore(
                any(ServiceProvider.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(timeSlots);

        final Map<LocalDate, List<ScheduleIntervalDto>> actualSchedule = scheduleService.getScheduleForServiceProvider(
                serviceProviderId,
                LocalDate.parse("2021-03-01"),
                LocalDate.parse("2021-03-02"));

        final ReservationDto rDTO1 = createReservationDto(r1);
        final ReservationDto rDTO2 = createReservationDto(r2);
        final ReservationDto rDTO3 = createReservationDto(r3);

        assertThat(actualSchedule).containsKeys(LocalDate.parse("2021-03-01"));
        assertThat(actualSchedule.get(LocalDate.parse("2021-03-01"))).containsExactly(
                new ScheduleIntervalDto(LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(60), null, IntervalStatus.UNAVAILABLE),
                new ScheduleIntervalDto(LocalTime.parse("09:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(30), rDTO1, IntervalStatus.RESERVED),
                new ScheduleIntervalDto(LocalTime.parse("09:30:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(2 * 60 + 30), null, IntervalStatus.UNAVAILABLE),
                new ScheduleIntervalDto(LocalTime.parse("12:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(30), rDTO2, IntervalStatus.RESERVED),
                new ScheduleIntervalDto(LocalTime.parse("12:30:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(45), rDTO3, IntervalStatus.RESERVED),
                new ScheduleIntervalDto(LocalTime.parse("13:15:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(7 * 60 + 45), null, IntervalStatus.UNAVAILABLE)
        );

    }

    @Test
    public void getScheduleForServiceProviderGroupsDifferentStatusSlots() {
        final Customer customer = new Customer(10L, "Foo Bar", "foo street", "foo@test.com", "12312341231");

        // DAY 1 01-03-2021
        // AVAILABLE INTERVAL 08:00 - 09:00
        final TimeSlot ts1 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts2 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts3 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:30:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts4 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:45:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);

        // R1 09:00 - 09:30
        final TimeSlot ts5 = new TimeSlot(LocalDateTime.parse("2021-03-01T09:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts6 = new TimeSlot(LocalDateTime.parse("2021-03-01T09:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final Reservation r1 = new Reservation(100L, ts5.getStart(), Duration.ofMinutes(30), ServiceType.TIRES_CHANGE,
                customer, Lists.newArrayList(ts5, ts6));
        ts5.setReservation(r1);
        ts6.setReservation(r1);

        // AVAILABLE INTERVAL 11:00 - 11:30
        final TimeSlot ts7 = new TimeSlot(LocalDateTime.parse("2021-03-01T11:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts8 = new TimeSlot(LocalDateTime.parse("2021-03-01T11:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);

        // R2 12:00 - 12:30
        final TimeSlot ts9 = new TimeSlot(LocalDateTime.parse("2021-03-01T12:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts10 = new TimeSlot(LocalDateTime.parse("2021-03-01T12:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final Reservation r2 = new Reservation(101L, ts9.getStart(), Duration.ofMinutes(30), ServiceType.TIRES_CHANGE,
                customer, Lists.newArrayList(ts9, ts10));
        ts9.setReservation(r2);
        ts10.setReservation(r2);

        List<TimeSlot> timeSlots = Lists.newArrayList(ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8, ts9, ts10);
        sp.setTimeSlots(timeSlots);
        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));
        when(timeSlotRepository.findByServiceProviderAndStartAfterAndStartBefore(
                any(ServiceProvider.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(timeSlots);

        final Map<LocalDate, List<ScheduleIntervalDto>> actualSchedule = scheduleService.getScheduleForServiceProvider(
                serviceProviderId,
                LocalDate.parse("2021-03-01"),
                LocalDate.parse("2021-03-02"));

        final ReservationDto rDTO1 = createReservationDto(r1);
        final ReservationDto rDTO2 = createReservationDto(r2);

        assertThat(actualSchedule).containsKeys(LocalDate.parse("2021-03-01"));
        assertThat(actualSchedule.get(LocalDate.parse("2021-03-01"))).containsExactly(
                new ScheduleIntervalDto(LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(60), null, IntervalStatus.AVAILABLE),
                new ScheduleIntervalDto(LocalTime.parse("09:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(30), rDTO1, IntervalStatus.RESERVED),
                new ScheduleIntervalDto(LocalTime.parse("09:30:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(90), null, IntervalStatus.UNAVAILABLE),
                new ScheduleIntervalDto(LocalTime.parse("11:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(30), null, IntervalStatus.AVAILABLE),
                new ScheduleIntervalDto(LocalTime.parse("11:30:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(30), null, IntervalStatus.UNAVAILABLE),
                new ScheduleIntervalDto(LocalTime.parse("12:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(30), rDTO2, IntervalStatus.RESERVED),
                new ScheduleIntervalDto(LocalTime.parse("12:30:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(8 * 60 + 30), null, IntervalStatus.UNAVAILABLE)
        );
    }

    @Test
    public void getScheduleForServiceProviderGroupsMultipleDaysSlots() {
        final Customer customer = new Customer(10L, "Foo Bar", "foo street", "foo@test.com", "12312341231");

        // DAY 1 01-03-2021
        // AVAILABLE INTERVAL 08:00 - 09:00
        final TimeSlot ts1 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts2 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts3 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:30:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts4 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:45:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);

        // R1 09:00 - 09:30
        final TimeSlot ts5 = new TimeSlot(LocalDateTime.parse("2021-03-01T09:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final TimeSlot ts6 = new TimeSlot(LocalDateTime.parse("2021-03-01T09:15:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final Reservation r1 = new Reservation(100L, ts5.getStart(), Duration.ofMinutes(30), ServiceType.TIRES_CHANGE,
                customer, Lists.newArrayList(ts5, ts6));
        ts5.setReservation(r1);
        ts6.setReservation(r1);

        // DAY 2 02-03-2021
        // R4 08:00 - 08:15
        final TimeSlot ts7 = new TimeSlot(LocalDateTime.parse("2021-03-02T08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);
        final Reservation r2 = new Reservation(103L, ts7.getStart(), Duration.ofMinutes(15), ServiceType.TIRES_CHANGE,
                customer, Lists.newArrayList(ts7));
        ts7.setReservation(r2);

        // AVAILABLE INTERVAL 09:00 - 09:15
        final TimeSlot ts8 = new TimeSlot(LocalDateTime.parse("2021-03-02T09:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);

        List<TimeSlot> timeSlots = Lists.newArrayList(ts1, ts2, ts3, ts4, ts5, ts6, ts7, ts8);
        sp.setTimeSlots(timeSlots);
        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));
        when(timeSlotRepository.findByServiceProviderAndStartAfterAndStartBefore(
                any(ServiceProvider.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(timeSlots);

        final Map<LocalDate, List<ScheduleIntervalDto>> actualSchedule = scheduleService.getScheduleForServiceProvider(
                serviceProviderId,
                LocalDate.parse("2021-03-01"),
                LocalDate.parse("2021-03-03"));

        final ReservationDto rDTO1 = createReservationDto(r1);
        final ReservationDto rDTO2 = createReservationDto(r2);

        assertThat(actualSchedule).containsKeys(LocalDate.parse("2021-03-01"), LocalDate.parse("2021-03-02"));
        assertThat(actualSchedule.get(LocalDate.parse("2021-03-01"))).containsExactly(
                new ScheduleIntervalDto(LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(60), null, IntervalStatus.AVAILABLE),
                new ScheduleIntervalDto(LocalTime.parse("09:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(30), rDTO1, IntervalStatus.RESERVED),
                new ScheduleIntervalDto(LocalTime.parse("09:30:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(11 * 60 + 30), null, IntervalStatus.UNAVAILABLE)
        );
        assertThat(actualSchedule.get(LocalDate.parse("2021-03-02"))).containsExactly(
                new ScheduleIntervalDto(LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(15), rDTO2, IntervalStatus.RESERVED),
                new ScheduleIntervalDto(LocalTime.parse("08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(45), null, IntervalStatus.UNAVAILABLE),
                new ScheduleIntervalDto(LocalTime.parse("09:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(15), null, IntervalStatus.AVAILABLE),
                new ScheduleIntervalDto(LocalTime.parse("09:15:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(11 * 60 + 45), null, IntervalStatus.UNAVAILABLE)
        );
    }

    @Test
    public void getScheduleForServiceProviderHandlesNoTimeSlots() {
        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));
        when(timeSlotRepository.findByServiceProviderAndStartAfterAndStartBefore(
                any(ServiceProvider.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Lists.newArrayList());

        final Map<LocalDate, List<ScheduleIntervalDto>> actualSchedule = scheduleService.getScheduleForServiceProvider(
                serviceProviderId,
                LocalDate.parse("2021-03-01"),
                LocalDate.parse("2021-03-03"));

        assertThat(actualSchedule).containsKeys(LocalDate.parse("2021-03-01"), LocalDate.parse("2021-03-02"));
        assertThat(actualSchedule.get(LocalDate.parse("2021-03-01"))).containsExactly(
                new ScheduleIntervalDto(LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                Duration.ofMinutes(13 * 60), null, IntervalStatus.UNAVAILABLE));
        assertThat(actualSchedule.get(LocalDate.parse("2021-03-02"))).containsExactly(
                new ScheduleIntervalDto(LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(13 * 60), null, IntervalStatus.UNAVAILABLE));
    }

    @Test
    public void getScheduleForServiceProviderHandlesOneTimeSlot() {
        // DAY 1 01-03-2021
        // AVAILABLE INTERVAL 08:00 - 08:15
        final TimeSlot ts1 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                sp, duration, null);

        List<TimeSlot> timeSlots = Lists.newArrayList(ts1);
        sp.setTimeSlots(timeSlots);
        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));
        when(timeSlotRepository.findByServiceProviderAndStartAfterAndStartBefore(
                any(ServiceProvider.class), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(timeSlots);

        final Map<LocalDate, List<ScheduleIntervalDto>> actualSchedule = scheduleService.getScheduleForServiceProvider(
                serviceProviderId,
                LocalDate.parse("2021-03-01"),
                LocalDate.parse("2021-03-02"));

        assertThat(actualSchedule).containsKeys(LocalDate.parse("2021-03-01"));
        assertThat(actualSchedule.get(LocalDate.parse("2021-03-01"))).containsExactly(
                new ScheduleIntervalDto(LocalTime.parse("08:00:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(15), null, IntervalStatus.AVAILABLE),
                new ScheduleIntervalDto(LocalTime.parse("08:15:00+02:00", DateTimeFormatter.ISO_OFFSET_TIME),
                        Duration.ofMinutes(12 * 60 + 45), null, IntervalStatus.UNAVAILABLE)
        );
    }

    private ReservationDto createReservationDto(final Reservation reservation) {
        return new ReservationDto(
                reservation.getId(),
                reservation.getStart(),
                reservation.getDuration(),
                reservation.getServiceType(),
                reservation.getCustomer().getName(),
                reservation.getCustomer().getAddress(),
                reservation.getCustomer().getEmail(),
                reservation.getCustomer().getPhoneNumber()
        );
    }
}
