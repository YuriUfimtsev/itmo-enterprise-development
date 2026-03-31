package ru.itmo.ipkn.yuriufimtsev.service.member.policy;

import org.springframework.stereotype.Component;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.RatePlanSnapshot;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.Subscription;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class TimeWindowPolicy implements AccessPolicy {

    @Override
    public boolean isAllowed(Subscription subscription, RatePlanSnapshot ratePlan,
                             LocalDateTime now, int weeklyVisitsLast7Days) {
        LocalTime currentTime = now.toLocalTime();
        return !currentTime.isBefore(ratePlan.getTimeFrom())
                && !currentTime.isAfter(ratePlan.getTimeTo());
    }
}
