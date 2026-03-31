package ru.itmo.ipkn.yuriufimtsev.service.rateplan.dto;

import lombok.*;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.PolicyType;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatePlanCreateRequest {
    private PolicyType policyType;
    private Integer daysDuration;
    private BigDecimal price;
    private LocalTime timeFrom;
    private LocalTime timeTo;
    private Integer weeklyLimit;
}
