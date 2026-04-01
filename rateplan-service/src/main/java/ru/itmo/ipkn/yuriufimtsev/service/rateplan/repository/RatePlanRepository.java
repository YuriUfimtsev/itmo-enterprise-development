package ru.itmo.ipkn.yuriufimtsev.service.rateplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.RatePlan;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatePlanRepository extends JpaRepository<RatePlan, Long> {

    Optional<RatePlan> findByIdAndIsActualTrue(Long id);

    @Query(value = """
            SELECT * FROM rate_plans r WHERE r.is_actual = true
            AND r.policy_type = :policyType
            AND r.days_duration = :daysDuration
            AND (CAST(:timeFrom AS time) IS NULL AND r.time_from IS NULL OR r.time_from = CAST(:timeFrom AS time))
            AND (CAST(:timeTo AS time) IS NULL AND r.time_to IS NULL OR r.time_to = CAST(:timeTo AS time))
            AND (CAST(:weeklyLimit AS integer) IS NULL AND r.weekly_limit IS NULL OR r.weekly_limit = CAST(:weeklyLimit AS integer))
            """, nativeQuery = true)
    Optional<RatePlan> findDuplicateActual(String policyType, Integer daysDuration,
                                           LocalTime timeFrom, LocalTime timeTo,
                                           Integer weeklyLimit);

    @Query(value = """
            SELECT * FROM rate_plans r WHERE r.is_actual = true
            AND (CAST(:policyType AS varchar) IS NULL OR r.policy_type = CAST(:policyType AS varchar))
            AND (CAST(:daysDuration AS integer) IS NULL OR r.days_duration = CAST(:daysDuration AS integer))
            AND (CAST(:timeFrom AS time) IS NULL OR r.time_from = CAST(:timeFrom AS time))
            AND (CAST(:timeTo AS time) IS NULL OR r.time_to = CAST(:timeTo AS time))
            AND (CAST(:weeklyLimit AS integer) IS NULL OR r.weekly_limit = CAST(:weeklyLimit AS integer))
            """, nativeQuery = true)
    List<RatePlan> searchActual(String policyType, Integer daysDuration,
                                LocalTime timeFrom, LocalTime timeTo,
                                Integer weeklyLimit);
}
