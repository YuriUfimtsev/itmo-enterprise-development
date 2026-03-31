package ru.itmo.ipkn.yuriufimtsev.service.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.PolicyType;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.RatePlanSnapshot;
import ru.itmo.ipkn.yuriufimtsev.service.member.repository.RatePlanSnapshotRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RatePlanEventConsumer {

    private final RatePlanSnapshotRepository ratePlanSnapshotRepository;
    private final ObjectMapper objectMapper;

    public void handleRatePlanUpsert(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);

            RatePlanSnapshot snapshot = ratePlanSnapshotRepository
                    .findById(node.get("id").asLong())
                    .orElse(new RatePlanSnapshot());

            snapshot.setId(node.get("id").asLong());
            snapshot.setPolicyType(PolicyType.valueOf(node.get("policyType").asText()));
            snapshot.setDaysDuration(node.get("daysDuration").asInt());
            snapshot.setPrice(new BigDecimal(node.get("price").asText()));
            snapshot.setActual(node.get("actual").asBoolean());

            if (node.has("timeFrom") && !node.get("timeFrom").isNull()) {
                snapshot.setTimeFrom(LocalTime.parse(node.get("timeFrom").asText()));
            }
            if (node.has("timeTo") && !node.get("timeTo").isNull()) {
                snapshot.setTimeTo(LocalTime.parse(node.get("timeTo").asText()));
            }
            if (node.has("weeklyLimit") && !node.get("weeklyLimit").isNull()) {
                snapshot.setWeeklyLimit(node.get("weeklyLimit").asInt());
            }

            snapshot.setUpdatedAt(LocalDateTime.now());
            ratePlanSnapshotRepository.save(snapshot);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse rate plan event payload", e);
        }
    }

    public void handleRatePlanDeactivate(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            Long id = node.get("id").asLong();
            ratePlanSnapshotRepository.deleteById(id);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse rate plan deactivate payload", e);
        }
    }
}
