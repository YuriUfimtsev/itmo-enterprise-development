package ru.itmo.ipkn.yuriufimtsev.service.member.policy;

import ru.itmo.ipkn.yuriufimtsev.service.member.model.RatePlanSnapshot;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.Subscription;

import java.time.LocalDateTime;

public interface AccessPolicy {

    boolean isAllowed(Subscription subscription, RatePlanSnapshot ratePlan,
                      LocalDateTime now, int weeklyVisitsLast7Days);
}
