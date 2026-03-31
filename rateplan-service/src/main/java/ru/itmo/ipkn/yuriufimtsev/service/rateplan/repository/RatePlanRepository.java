package ru.itmo.ipkn.yuriufimtsev.service.rateplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.PolicyType;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.RatePlan;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatePlanRepository extends JpaRepository<RatePlan, Long> {

    Optional<RatePlan> findByIdAndIsActualTrue(Long id);

    @Query("""
            SELECT r FROM RatePlan r WHERE r.isActual = true
            AND r.policyType = :policyType
            AND r.daysDuration = :daysDuration
            AND (:timeFrom IS NULL AND r.timeFrom IS NULL OR r.timeFrom = :timeFrom)
            AND (:timeTo IS NULL AND r.timeTo IS NULL OR r.timeTo = :timeTo)
            AND (:weeklyLimit IS NULL AND r.weeklyLimit IS NULL OR r.weeklyLimit = :weeklyLimit)
            """)
    Optional<RatePlan> findDuplicateActual(PolicyType policyType, Integer daysDuration,
                                           LocalTime timeFrom, LocalTime timeTo,
                                           Integer weeklyLimit);

    @Query("""
            SELECT r FROM RatePlan r WHERE r.isActual = true
            AND (:policyType IS NULL OR r.policyType = :policyType)
            AND (:daysDuration IS NULL OR r.daysDuration = :daysDuration)
            AND (:timeFrom IS NULL OR r.timeFrom = :timeFrom)
            AND (:timeTo IS NULL OR r.timeTo = :timeTo)
            AND (:weeklyLimit IS NULL OR r.weeklyLimit = :weeklyLimit)
            """)
    List<RatePlan> searchActual(PolicyType policyType, Integer daysDuration,
                                LocalTime timeFrom, LocalTime timeTo,
                                Integer weeklyLimit);
}
