package ru.itmo.ipkn.yuriufimtsev.service.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.Subscription;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT s FROM Subscription s WHERE s.memberId = :memberId AND s.to >= :today")
    Optional<Subscription> findActiveByMemberId(Long memberId, LocalDate today);
}
