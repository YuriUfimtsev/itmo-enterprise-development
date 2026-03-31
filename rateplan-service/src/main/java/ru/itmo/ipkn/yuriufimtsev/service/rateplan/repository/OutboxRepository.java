package ru.itmo.ipkn.yuriufimtsev.service.rateplan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.OutboxEvent;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.OutboxStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
