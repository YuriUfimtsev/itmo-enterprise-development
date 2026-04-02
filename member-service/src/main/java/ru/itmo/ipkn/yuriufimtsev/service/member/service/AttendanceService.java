package ru.itmo.ipkn.yuriufimtsev.service.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.*;
import ru.itmo.ipkn.yuriufimtsev.service.member.policy.AccessPolicy;
import ru.itmo.ipkn.yuriufimtsev.service.member.policy.TimeWindowPolicy;
import ru.itmo.ipkn.yuriufimtsev.service.member.policy.UnlimitedPolicy;
import ru.itmo.ipkn.yuriufimtsev.service.member.policy.WeeklyLimitPolicy;
import ru.itmo.ipkn.yuriufimtsev.service.member.repository.AttendanceRepository;
import ru.itmo.ipkn.yuriufimtsev.service.member.repository.MemberRepository;
import ru.itmo.ipkn.yuriufimtsev.service.member.repository.RatePlanSnapshotRepository;
import ru.itmo.ipkn.yuriufimtsev.service.member.repository.SubscriptionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AttendanceRepository attendanceRepository;
    private final RatePlanSnapshotRepository ratePlanSnapshotRepository;

    private final UnlimitedPolicy unlimitedPolicy;
    private final TimeWindowPolicy timeWindowPolicy;
    private final WeeklyLimitPolicy weeklyLimitPolicy;

    private static final Map<PolicyType, String> DENIAL_REASONS = Map.of(
            PolicyType.TimeWindow, "Текущее время не попадает в интервал доступа посетителя",
            PolicyType.WeeklyLimit, "Количество доступных посещений в течение недели исчерпано"
    );

    @Transactional
    public Attendance registerAttendance(Long memberId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Посетитель с идентификатором " + memberId + " не найден"));

        Subscription subscription = subscriptionRepository
                .findActiveByMemberId(memberId, today)
                .orElse(null);

        if (subscription == null || !subscription.isValid(today)) {
            return attendanceRepository.save(
                    new Attendance(memberId, now, false, "Не найден актуальный абонемент посетителя"));
        }

        RatePlanSnapshot ratePlan = ratePlanSnapshotRepository.findById(subscription.getRatePlanId())
                .orElseThrow(() -> new IllegalStateException(
                        "Снимок тарифного плана " + subscription.getRatePlanId() + " не найден"));

        int weeklyVisits = attendanceRepository.countAllowedVisitsLast7Days(memberId, now.minusDays(7));

        AccessPolicy policy = resolvePolicy(ratePlan.getPolicyType());
        boolean allowed = policy.isAllowed(subscription, ratePlan, now, weeklyVisits);

        String reason = allowed ? "" : DENIAL_REASONS.getOrDefault(ratePlan.getPolicyType(), "");
        return attendanceRepository.save(new Attendance(memberId, now, allowed, reason));
    }

    private AccessPolicy resolvePolicy(PolicyType type) {
        return switch (type) {
            case Unlimited -> unlimitedPolicy;
            case TimeWindow -> timeWindowPolicy;
            case WeeklyLimit -> weeklyLimitPolicy;
        };
    }
}
