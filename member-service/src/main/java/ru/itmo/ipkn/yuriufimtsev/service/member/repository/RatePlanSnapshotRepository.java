package ru.itmo.ipkn.yuriufimtsev.service.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.RatePlanSnapshot;

@Repository
public interface RatePlanSnapshotRepository extends JpaRepository<RatePlanSnapshot, Long> {
}
