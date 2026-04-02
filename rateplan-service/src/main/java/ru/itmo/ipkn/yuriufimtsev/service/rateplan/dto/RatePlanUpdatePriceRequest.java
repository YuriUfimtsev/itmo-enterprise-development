package ru.itmo.ipkn.yuriufimtsev.service.rateplan.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RatePlanUpdatePriceRequest {
    private Long ratePlanId;
    private BigDecimal newPrice;
}
