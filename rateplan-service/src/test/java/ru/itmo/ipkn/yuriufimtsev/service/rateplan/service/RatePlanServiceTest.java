package ru.itmo.ipkn.yuriufimtsev.service.rateplan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.dto.RatePlanCreateRequest;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.dto.RatePlanSearchRequest;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.OutboxEvent;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.PolicyType;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.RatePlan;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.repository.OutboxRepository;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.repository.RatePlanRepository;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatePlanServiceTest {

    @Mock
    private RatePlanRepository ratePlanRepository;

    @Mock
    private OutboxRepository outboxRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private RatePlanService ratePlanService;

    @Test
    void createRatePlan_unlimited_returnsCreatedPlan() {
        var request = RatePlanCreateRequest.builder()
                .policyType(PolicyType.Unlimited)
                .daysDuration(30)
                .price(BigDecimal.valueOf(5000))
                .build();

        when(ratePlanRepository.findDuplicateActual(any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        var saved = RatePlan.builder()
                .id(1L)
                .policyType(PolicyType.Unlimited)
                .daysDuration(30)
                .price(BigDecimal.valueOf(5000))
                .build();
        when(ratePlanRepository.save(any(RatePlan.class))).thenReturn(saved);
        when(outboxRepository.save(any(OutboxEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        RatePlan result = ratePlanService.createRatePlan(request);

        assertEquals(1L, result.getId());
        assertEquals(PolicyType.Unlimited, result.getPolicyType());
        verify(outboxRepository).save(any(OutboxEvent.class));
    }

    @Test
    void createRatePlan_timeWindow_returnsCreatedPlan() {
        var request = RatePlanCreateRequest.builder()
                .policyType(PolicyType.TimeWindow)
                .daysDuration(30)
                .price(BigDecimal.valueOf(3000))
                .timeFrom(LocalTime.of(8, 0))
                .timeTo(LocalTime.of(14, 0))
                .build();

        when(ratePlanRepository.findDuplicateActual(any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        var saved = RatePlan.builder()
                .id(2L)
                .policyType(PolicyType.TimeWindow)
                .daysDuration(30)
                .price(BigDecimal.valueOf(3000))
                .timeFrom(LocalTime.of(8, 0))
                .timeTo(LocalTime.of(14, 0))
                .build();
        when(ratePlanRepository.save(any(RatePlan.class))).thenReturn(saved);
        when(outboxRepository.save(any(OutboxEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        RatePlan result = ratePlanService.createRatePlan(request);

        assertEquals(PolicyType.TimeWindow, result.getPolicyType());
        assertEquals(LocalTime.of(8, 0), result.getTimeFrom());
    }

    @Test
    void createRatePlan_weeklyLimit_returnsCreatedPlan() {
        var request = RatePlanCreateRequest.builder()
                .policyType(PolicyType.WeeklyLimit)
                .daysDuration(30)
                .price(BigDecimal.valueOf(2000))
                .weeklyLimit(3)
                .build();

        when(ratePlanRepository.findDuplicateActual(any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        var saved = RatePlan.builder()
                .id(3L)
                .policyType(PolicyType.WeeklyLimit)
                .daysDuration(30)
                .price(BigDecimal.valueOf(2000))
                .weeklyLimit(3)
                .build();
        when(ratePlanRepository.save(any(RatePlan.class))).thenReturn(saved);
        when(outboxRepository.save(any(OutboxEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        RatePlan result = ratePlanService.createRatePlan(request);

        assertEquals(PolicyType.WeeklyLimit, result.getPolicyType());
        assertEquals(3, result.getWeeklyLimit());
    }

    @Test
    void createRatePlan_duplicateExists_throwsException() {
        var request = RatePlanCreateRequest.builder()
                .policyType(PolicyType.Unlimited)
                .daysDuration(30)
                .price(BigDecimal.valueOf(5000))
                .build();
        var existing = RatePlan.builder().id(42L).build();
        when(ratePlanRepository.findDuplicateActual(any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(existing));

        var ex = assertThrows(IllegalArgumentException.class, () ->
                ratePlanService.createRatePlan(request));

        assertTrue(ex.getMessage().contains("42"));
    }

    @ParameterizedTest(name = "createRatePlan_{0}_throwsException")
    @MethodSource("invalidPolicyParams")
    void createRatePlan_invalidParams_throwsException(String scenario, RatePlanCreateRequest request) {
        assertThrows(IllegalArgumentException.class, () ->
                ratePlanService.createRatePlan(request));
    }

    static Stream<Arguments> invalidPolicyParams() {
        return Stream.of(
                Arguments.of("timeWindowWithoutTimeRange",
                        RatePlanCreateRequest.builder()
                                .policyType(PolicyType.TimeWindow)
                                .daysDuration(30)
                                .price(BigDecimal.valueOf(3000))
                                .build()),
                Arguments.of("timeWindowWithExtraWeeklyLimit",
                        RatePlanCreateRequest.builder()
                                .policyType(PolicyType.TimeWindow)
                                .daysDuration(30)
                                .price(BigDecimal.valueOf(3000))
                                .timeFrom(LocalTime.of(8, 0))
                                .timeTo(LocalTime.of(14, 0))
                                .weeklyLimit(3)
                                .build()),
                Arguments.of("unlimitedWithExtraWeeklyLimit",
                        RatePlanCreateRequest.builder()
                                .policyType(PolicyType.Unlimited)
                                .daysDuration(30)
                                .price(BigDecimal.valueOf(5000))
                                .weeklyLimit(3)
                                .build()),
                Arguments.of("unlimitedWithExtraTimeRange",
                        RatePlanCreateRequest.builder()
                                .policyType(PolicyType.Unlimited)
                                .daysDuration(30)
                                .price(BigDecimal.valueOf(5000))
                                .timeFrom(LocalTime.of(8, 0))
                                .timeTo(LocalTime.of(14, 0))
                                .build()),
                Arguments.of("weeklyLimitWithoutLimit",
                        RatePlanCreateRequest.builder()
                                .policyType(PolicyType.WeeklyLimit)
                                .daysDuration(30)
                                .price(BigDecimal.valueOf(2000))
                                .build()),
                Arguments.of("weeklyLimitWithExtraTimeRange",
                        RatePlanCreateRequest.builder()
                                .policyType(PolicyType.WeeklyLimit)
                                .daysDuration(30)
                                .price(BigDecimal.valueOf(2000))
                                .weeklyLimit(3)
                                .timeFrom(LocalTime.of(8, 0))
                                .timeTo(LocalTime.of(14, 0))
                                .build())
        );
    }

    @Test
    void updatePrice_existingPlan_updatesPrice() {
        var ratePlan = RatePlan.builder()
                .id(1L)
                .policyType(PolicyType.Unlimited)
                .daysDuration(30)
                .price(BigDecimal.valueOf(5000))
                .build();
        when(ratePlanRepository.findByIdAndIsActualTrue(1L)).thenReturn(Optional.of(ratePlan));
        when(ratePlanRepository.save(any(RatePlan.class))).thenReturn(ratePlan);
        when(outboxRepository.save(any(OutboxEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        ratePlanService.updatePrice(1L, BigDecimal.valueOf(6000));

        assertEquals(BigDecimal.valueOf(6000), ratePlan.getPrice());
        verify(outboxRepository).save(any(OutboxEvent.class));
    }

    @Test
    void updatePrice_notFound_throwsException() {
        when(ratePlanRepository.findByIdAndIsActualTrue(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                ratePlanService.updatePrice(99L, BigDecimal.valueOf(6000)));
    }

    @Test
    void deleteRatePlan_existingPlan_setsIsActualFalse() {
        var ratePlan = RatePlan.builder()
                .id(1L)
                .policyType(PolicyType.Unlimited)
                .daysDuration(30)
                .price(BigDecimal.valueOf(5000))
                .isActual(true)
                .build();
        when(ratePlanRepository.findByIdAndIsActualTrue(1L)).thenReturn(Optional.of(ratePlan));
        when(ratePlanRepository.save(any(RatePlan.class))).thenReturn(ratePlan);
        when(outboxRepository.save(any(OutboxEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        ratePlanService.deleteRatePlan(1L);

        assertFalse(ratePlan.isActual());
        verify(outboxRepository).save(any(OutboxEvent.class));
    }

    @Test
    void deleteRatePlan_notFound_throwsException() {
        when(ratePlanRepository.findByIdAndIsActualTrue(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                ratePlanService.deleteRatePlan(99L));
    }

    @Test
    void searchRatePlans_byPolicyType_returnsFilteredResults() {
        var request = RatePlanSearchRequest.builder()
                .policyType(PolicyType.TimeWindow)
                .build();
        var plan = RatePlan.builder()
                .id(1L)
                .policyType(PolicyType.TimeWindow)
                .timeFrom(LocalTime.of(8, 0))
                .timeTo(LocalTime.of(12, 0))
                .build();
        when(ratePlanRepository.searchActual("TimeWindow", null, null, null, null))
                .thenReturn(List.of(plan));

        List<RatePlan> results = ratePlanService.searchRatePlans(request);

        assertEquals(1, results.size());
        assertEquals(PolicyType.TimeWindow, results.get(0).getPolicyType());
    }
}
