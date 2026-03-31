package ru.itmo.ipkn.yuriufimtsev.service.member.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "rate_plan_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatePlanSnapshot {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyType policyType;

    @Column(nullable = false)
    private Integer daysDuration;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean isActual;

    private LocalTime timeFrom;

    private LocalTime timeTo;

    private Integer weeklyLimit;

    private LocalDateTime updatedAt;
}
