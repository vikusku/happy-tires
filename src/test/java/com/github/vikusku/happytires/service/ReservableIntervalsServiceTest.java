package com.github.vikusku.happytires.service;

import com.github.vikusku.happytires.dto.ReservableIntervalDto;
import com.github.vikusku.happytires.model.ServiceProvider;
import com.github.vikusku.happytires.model.ServiceType;
import com.github.vikusku.happytires.model.TimeSlot;
import com.github.vikusku.happytires.repository.ServiceProviderRepository;
import com.github.vikusku.happytires.repository.TimeSlotRepository;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.vikusku.happytires.util.Constants.DEFAULT_TIME_SLOT_DURATION;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.when;

@WebMvcTest(ReservableIntervalsServiceTest.class)
class ReservableIntervalsServiceTest {

    @MockBean
    private TimeSlotRepository timeSlotRepository;

    @MockBean
    private ServiceProviderRepository spRepository;

    @MockBean
    private DurationService durationService;

    @Mock
    private ServiceProvider sp;

    private long serviceProviderId = 1L;
    private ReservableIntervalsService reservableIntervalsService;

    @BeforeEach
    public void setUp() {
        reservableIntervalsService = new ReservableIntervalsService(timeSlotRepository, spRepository, durationService);
        when(sp.getId()).thenReturn(serviceProviderId);
    }

    @AfterEach
    void tearDown() {
        clearInvocations(timeSlotRepository, spRepository);
    }


    @Test
    public void findReservableIntervalsForOneDay() {
        Duration reservableIntervalDuration = Duration.ofMinutes(30);
        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));
        when(durationService.getServiceDuration(any(ServiceType.class))).thenReturn(30);

        final TimeSlot slot1 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot2 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot3 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:30:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot4 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:45:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot5 = new TimeSlot(LocalDateTime.parse("2021-03-01T10:30:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot6 = new TimeSlot(LocalDateTime.parse("2021-03-01T10:45:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot7 = new TimeSlot(LocalDateTime.parse("2021-03-01T19:00:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);

        when(timeSlotRepository.findByServiceProviderAndStartAfterAndStartBeforeAndReservationIsNull(
                eq(sp), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(
                Lists.newArrayList(slot1, slot2, slot3, slot4, slot5, slot6, slot7)
        );

        Map<LocalDate, List<ReservableIntervalDto>>  reservableIntervals = reservableIntervalsService.findReservableIntervals(
                serviceProviderId,
                ServiceType.TIRES_CHANGE,
                LocalDate.parse("2021-03-01"),
                LocalDate.parse("2021-03-02"));

        assertThat(reservableIntervals).containsKeys(LocalDate.parse("2021-03-01"));
        assertThat(reservableIntervals.get(LocalDate.parse("2021-03-01"))).containsExactly(
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T08:00:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId),
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T08:15:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId),
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T08:30:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId),
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T10:30:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId)
        );
    }

    @Test
    public void findReservableIntervalsForTwoDays() {
        Duration reservableIntervalDuration = Duration.ofMinutes(30);
        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));
        when(durationService.getServiceDuration(any(ServiceType.class))).thenReturn(30);

        // DAY 1
        final TimeSlot slot1 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot2 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot3 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:30:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot4 = new TimeSlot(LocalDateTime.parse("2021-03-01T10:30:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot5 = new TimeSlot(LocalDateTime.parse("2021-03-01T10:45:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot6 = new TimeSlot(LocalDateTime.parse("2021-03-01T19:00:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);

        // DAY 2
        final TimeSlot slot7 = new TimeSlot(LocalDateTime.parse("2021-03-02T08:00:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot8 = new TimeSlot(LocalDateTime.parse("2021-03-02T08:15:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot9 = new TimeSlot(LocalDateTime.parse("2021-03-02T08:45:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot10 = new TimeSlot(LocalDateTime.parse("2021-03-02T10:30:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);

        when(timeSlotRepository.findByServiceProviderAndStartAfterAndStartBeforeAndReservationIsNull(
                eq(sp), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(
                Lists.newArrayList(slot1, slot2, slot3, slot4, slot5, slot6, slot7, slot8, slot9, slot10)
        );

        Map<LocalDate, List<ReservableIntervalDto>>  reservableIntervals = reservableIntervalsService.findReservableIntervals(
                serviceProviderId,
                ServiceType.TIRES_CHANGE,
                LocalDate.parse("2021-03-01"),
                LocalDate.parse("2021-03-03"));

        assertThat(reservableIntervals).containsKeys(LocalDate.parse("2021-03-01"), LocalDate.parse("2021-03-02"));
        assertThat(reservableIntervals.get(LocalDate.parse("2021-03-01"))).containsExactly(
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T08:00:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId),
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T08:15:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId),
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T10:30:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId)
        );

        assertThat(reservableIntervals.get(LocalDate.parse("2021-03-02"))).containsExactly(
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-02T08:00:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId)
        );
    }

    @Test
    public void findReservableIntervalsWithNoAvailableTimeSlots() {
        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));
        when(durationService.getServiceDuration(any(ServiceType.class))).thenReturn(30);

        when(timeSlotRepository.findByServiceProviderAndStartAfterAndStartBeforeAndReservationIsNull(
                eq(sp), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Lists.newArrayList());

        Map<LocalDate, List<ReservableIntervalDto>>  reservableIntervals = reservableIntervalsService.findReservableIntervals(
                serviceProviderId,
                ServiceType.TIRES_CHANGE,
                LocalDate.parse("2021-03-01"),
                LocalDate.parse("2021-03-03"));

        assertThat(reservableIntervals).containsKeys(LocalDate.parse("2021-03-01"), LocalDate.parse("2021-03-02"));
        assertThat(reservableIntervals.get(LocalDate.parse("2021-03-01"))).isEmpty();
        assertThat(reservableIntervals.get(LocalDate.parse("2021-03-02"))).isEmpty();
    }

    @Test
    public void findReservableIntervalsNoAdjacentAvailableTimeSlots() {
        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));
        when(durationService.getServiceDuration(any(ServiceType.class))).thenReturn(30);

        final TimeSlot slot1 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot2 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:30:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot3 = new TimeSlot(LocalDateTime.parse("2021-03-01T09:30:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot4 = new TimeSlot(LocalDateTime.parse("2021-03-01T14:45:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);

        when(timeSlotRepository.findByServiceProviderAndStartAfterAndStartBeforeAndReservationIsNull(
                eq(sp), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Lists.newArrayList(
                        slot1, slot2, slot3, slot4
        ));

        Map<LocalDate, List<ReservableIntervalDto>>  reservableIntervals = reservableIntervalsService.findReservableIntervals(
                serviceProviderId,
                ServiceType.TIRES_CHANGE,
                LocalDate.parse("2021-03-01"),
                LocalDate.parse("2021-03-02"));

        assertThat(reservableIntervals).containsKeys(LocalDate.parse("2021-03-01"));
        assertThat(reservableIntervals.get(LocalDate.parse("2021-03-01"))).isEmpty();
    }

    @Test
    public void findReservableIntervalsWith45MinutesServiceDuration() {
        Duration reservableIntervalDuration = Duration.ofMinutes(45);
        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));
        when(durationService.getServiceDuration(any(ServiceType.class))).thenReturn(45);

        final TimeSlot slot1 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot2 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot3 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:30:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot4 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:45:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot5 = new TimeSlot(LocalDateTime.parse("2021-03-01T09:00:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot6 = new TimeSlot(LocalDateTime.parse("2021-03-01T11:00:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot7 = new TimeSlot(LocalDateTime.parse("2021-03-01T11:15:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot8 = new TimeSlot(LocalDateTime.parse("2021-03-01T11:30:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot9 = new TimeSlot(LocalDateTime.parse("2021-03-01T15:30:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot10 = new TimeSlot(LocalDateTime.parse("2021-03-01T15:45:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);


        when(timeSlotRepository.findByServiceProviderAndStartAfterAndStartBeforeAndReservationIsNull(
                eq(sp), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Lists.newArrayList(
                slot1, slot2, slot3, slot4, slot5, slot6, slot7, slot8, slot9, slot10
        ));

        Map<LocalDate, List<ReservableIntervalDto>>  reservableIntervals = reservableIntervalsService.findReservableIntervals(
                serviceProviderId,
                ServiceType.TIRES_CHANGE,
                LocalDate.parse("2021-03-01"),
                LocalDate.parse("2021-03-02"));

        assertThat(reservableIntervals).containsKeys(LocalDate.parse("2021-03-01"));
        assertThat(reservableIntervals.get(LocalDate.parse("2021-03-01"))).containsExactly(
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T08:00:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId),
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T08:15:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId),
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T08:30:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId),
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T11:00:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId)
        );

    }

    @Test
    public void findReservableIntervalsWith15MinutesServiceDuration() {
        Duration reservableIntervalDuration = Duration.ofMinutes(15);
        when(spRepository.findById(serviceProviderId)).thenReturn(Optional.of(sp));
        when(durationService.getServiceDuration(any(ServiceType.class))).thenReturn(15);

        final TimeSlot slot1 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:00:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot2 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:15:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot3 = new TimeSlot(LocalDateTime.parse("2021-03-01T08:30:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot4 = new TimeSlot(LocalDateTime.parse("2021-03-01T09:00:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot5 = new TimeSlot(LocalDateTime.parse("2021-03-01T11:00:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot6 = new TimeSlot(LocalDateTime.parse("2021-03-01T11:15:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);
        final TimeSlot slot7 = new TimeSlot(LocalDateTime.parse("2021-03-01T15:30:00+02:00", ISO_OFFSET_DATE_TIME),
                sp, DEFAULT_TIME_SLOT_DURATION, null);


        when(timeSlotRepository.findByServiceProviderAndStartAfterAndStartBeforeAndReservationIsNull(
                eq(sp), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(Lists.newArrayList(
                slot1, slot2, slot3, slot4, slot5, slot6, slot7
        ));

        Map<LocalDate, List<ReservableIntervalDto>>  reservableIntervals = reservableIntervalsService.findReservableIntervals(
                serviceProviderId,
                ServiceType.TIRES_CHANGE,
                LocalDate.parse("2021-03-01"),
                LocalDate.parse("2021-03-02"));

        assertThat(reservableIntervals).containsKeys(LocalDate.parse("2021-03-01"));
        assertThat(reservableIntervals.get(LocalDate.parse("2021-03-01"))).containsExactly(
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T08:00:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId),
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T08:15:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId),
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T08:30:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId),
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T09:00:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId),
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T11:00:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId),
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T11:15:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId),
                new ReservableIntervalDto(LocalDateTime.parse("2021-03-01T15:30:00+02:00", ISO_OFFSET_DATE_TIME), reservableIntervalDuration, serviceProviderId)
        );


    }

}