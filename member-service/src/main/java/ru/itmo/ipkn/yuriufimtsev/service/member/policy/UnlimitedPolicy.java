package ru.itmo.ipkn.yuriufimtsev.service.member.policy;

import org.springframework.stereotype.Component;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.RatePlanSnapshot;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.Subscription;

import java.time.LocalDateTime;

@Component
public class UnlimitedPolicy implements AccessPolicy {

    @Override
    public boolean isAllowed(Subscription subscription, RatePlanSnapshot ratePlan,
                             LocalDateTime now, int weeklyVisitsLast7Days) {
        return true;
    }
}
