package ru.itmo.ipkn.yuriufimtsev.service.rateplan.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.OutboxEvent;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.OutboxStatus;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.repository.OutboxRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final String TOPIC = "rateplan-events";

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.NEW);

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(TOPIC, event.getAggregateId(), event.getEventType() + ":" + event.getPayload());
                log.info("Published event to Kafka: type={}, aggregateId={}", event.getEventType(), event.getAggregateId());
                event.setStatus(OutboxStatus.SENT);
                outboxRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to publish event {}", event.getId(), e);
                event.setStatus(OutboxStatus.FAILED);
                outboxRepository.save(event);
            }
        }
    }
}
