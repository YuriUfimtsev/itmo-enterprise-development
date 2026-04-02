package ru.itmo.ipkn.yuriufimtsev.service.rateplan.dto;

import lombok.*;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.PolicyType;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatePlanSearchRequest {
    private PolicyType policyType;
    private Integer daysDuration;
    private LocalTime timeFrom;
    private LocalTime timeTo;
    private Integer weeklyLimit;
}
