package ru.itmo.ipkn.yuriufimtsev.service.member.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.*;
import ru.itmo.ipkn.yuriufimtsev.service.member.policy.TimeWindowPolicy;
import ru.itmo.ipkn.yuriufimtsev.service.member.policy.UnlimitedPolicy;
import ru.itmo.ipkn.yuriufimtsev.service.member.policy.WeeklyLimitPolicy;
import ru.itmo.ipkn.yuriufimtsev.service.member.repository.AttendanceRepository;
import ru.itmo.ipkn.yuriufimtsev.service.member.repository.MemberRepository;
import ru.itmo.ipkn.yuriufimtsev.service.member.repository.RatePlanSnapshotRepository;
import ru.itmo.ipkn.yuriufimtsev.service.member.repository.SubscriptionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private RatePlanSnapshotRepository ratePlanSnapshotRepository;
    @Mock private UnlimitedPolicy unlimitedPolicy;
    @Mock private TimeWindowPolicy timeWindowPolicy;
    @Mock private WeeklyLimitPolicy weeklyLimitPolicy;

    @InjectMocks
    private AttendanceService attendanceService;

    private Member createMember() {
        return Member.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .birthDate(LocalDate.of(2000, 1, 1))
                .email("user@test.com")
                .role(Role.MEMBER)
                .build();
    }

    private Subscription createSubscription(Long memberId, Long ratePlanId) {
        return Subscription.builder()
                .id(1L)
                .memberId(memberId)
                .ratePlanId(ratePlanId)
                .from(LocalDate.now().minusDays(5))
                .to(LocalDate.now().plusDays(25))
                .build();
    }

    private RatePlanSnapshot createSnapshot(PolicyType type) {
        var builder = RatePlanSnapshot.builder()
                .id(10L)
                .policyType(type)
                .daysDuration(30)
                .price(BigDecimal.valueOf(5000))
                .isActual(true);

        if (type == PolicyType.TimeWindow) {
            builder.timeFrom(LocalTime.of(8, 0)).timeTo(LocalTime.of(14, 0));
        }
        if (type == PolicyType.WeeklyLimit) {
            builder.weeklyLimit(3);
        }
        return builder.build();
    }

    private void stubMemberAndSubscription(PolicyType policyType) {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(createMember()));
        when(subscriptionRepository.findActiveByMemberId(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(createSubscription(1L, 10L)));
        when(ratePlanSnapshotRepository.findById(10L)).thenReturn(Optional.of(createSnapshot(policyType)));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void registerAttendance_memberNotFound_throwsException() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                attendanceService.registerAttendance(99L));
    }

    @Test
    void registerAttendance_noSubscription_returnsDenied() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(createMember()));
        when(subscriptionRepository.findActiveByMemberId(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        Attendance result = attendanceService.registerAttendance(1L);

        assertFalse(result.isAllowed());
        assertTrue(result.getReason().contains("абонемент"));
    }

    @Test
    void registerAttendance_unlimitedPolicy_returnsAllowed() {
        stubMemberAndSubscription(PolicyType.Unlimited);
        when(attendanceRepository.countAllowedVisitsLast7Days(eq(1L), any())).thenReturn(0);
        when(unlimitedPolicy.isAllowed(any(), any(), any(), anyInt())).thenReturn(true);

        Attendance result = attendanceService.registerAttendance(1L);

        assertTrue(result.isAllowed());
        assertEquals("", result.getReason());
    }

    @Test
    void registerAttendance_weeklyLimitNotExhausted_returnsAllowed() {
        stubMemberAndSubscription(PolicyType.WeeklyLimit);
        when(attendanceRepository.countAllowedVisitsLast7Days(eq(1L), any())).thenReturn(1);
        when(weeklyLimitPolicy.isAllowed(any(), any(), any(), eq(1))).thenReturn(true);

        Attendance result = attendanceService.registerAttendance(1L);

        assertTrue(result.isAllowed());
        assertEquals("", result.getReason());
    }

    @Test
    void registerAttendance_weeklyLimitExhausted_returnsDenied() {
        stubMemberAndSubscription(PolicyType.WeeklyLimit);
        when(attendanceRepository.countAllowedVisitsLast7Days(eq(1L), any())).thenReturn(3);
        when(weeklyLimitPolicy.isAllowed(any(), any(), any(), eq(3))).thenReturn(false);

        Attendance result = attendanceService.registerAttendance(1L);

        assertFalse(result.isAllowed());
        assertTrue(result.getReason().contains("исчерпано"));
    }

    @Test
    void registerAttendance_withinTimeWindow_returnsAllowed() {
        stubMemberAndSubscription(PolicyType.TimeWindow);
        when(attendanceRepository.countAllowedVisitsLast7Days(eq(1L), any())).thenReturn(0);
        when(timeWindowPolicy.isAllowed(any(), any(), any(), anyInt())).thenReturn(true);

        Attendance result = attendanceService.registerAttendance(1L);

        assertTrue(result.isAllowed());
        assertEquals("", result.getReason());
    }

    @Test
    void registerAttendance_outsideTimeWindow_returnsDenied() {
        stubMemberAndSubscription(PolicyType.TimeWindow);
        when(attendanceRepository.countAllowedVisitsLast7Days(eq(1L), any())).thenReturn(0);
        when(timeWindowPolicy.isAllowed(any(), any(), any(), anyInt())).thenReturn(false);

        Attendance result = attendanceService.registerAttendance(1L);

        assertFalse(result.isAllowed());
        assertTrue(result.getReason().contains("интервал"));
    }
}
