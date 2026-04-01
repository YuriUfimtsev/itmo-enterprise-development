package ru.itmo.ipkn.yuriufimtsev.service.rateplan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.dto.RatePlanCreateRequest;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.dto.RatePlanSearchRequest;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.OutboxEvent;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.PolicyType;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.RatePlan;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.repository.OutboxRepository;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.repository.RatePlanRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RatePlanService {

    private final RatePlanRepository ratePlanRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public RatePlan createRatePlan(RatePlanCreateRequest request) {
        validatePolicyParams(request);

        Optional<RatePlan> duplicate = ratePlanRepository.findDuplicateActual(
                request.getPolicyType(), request.getDaysDuration(),
                request.getTimeFrom(), request.getTimeTo(), request.getWeeklyLimit());

        if (duplicate.isPresent()) {
            throw new IllegalArgumentException(
                    "Тарифный план с указанными параметрами уже существует (id = "
                            + duplicate.get().getId()
                            + "). Пожалуйста, воспользуйтесь функцией обновления цены существующего тарифного плана");
        }

        RatePlan ratePlan = new RatePlan();
        ratePlan.setPolicyType(request.getPolicyType());
        ratePlan.setDaysDuration(request.getDaysDuration());
        ratePlan.setPrice(request.getPrice());
        ratePlan.setTimeFrom(request.getTimeFrom());
        ratePlan.setTimeTo(request.getTimeTo());
        ratePlan.setWeeklyLimit(request.getWeeklyLimit());

        RatePlan saved = ratePlanRepository.save(ratePlan);
        saveOutboxEvent(saved, "CREATED");
        return saved;
    }

    @Transactional
    public void updatePrice(Long ratePlanId, BigDecimal newPrice) {
        RatePlan ratePlan = findActiveOrThrow(ratePlanId);
        ratePlan.setPrice(newPrice);
        RatePlan saved = ratePlanRepository.save(ratePlan);
        saveOutboxEvent(saved, "UPDATED");
    }

    @Transactional
    public void deleteRatePlan(Long ratePlanId) {
        RatePlan ratePlan = findActiveOrThrow(ratePlanId);
        ratePlan.setActual(false);
        ratePlanRepository.save(ratePlan);
        saveOutboxEvent(ratePlan, "DEACTIVATED");
    }

    public RatePlan getRatePlan(Long id) {
        return findActiveOrThrow(id);
    }

    public List<RatePlan> searchRatePlans(RatePlanSearchRequest request) {
        return ratePlanRepository.searchActual(
                request.getPolicyType(), request.getDaysDuration(),
                request.getTimeFrom(), request.getTimeTo(), request.getWeeklyLimit());
    }

    private RatePlan findActiveOrThrow(Long id) {
        return ratePlanRepository.findByIdAndIsActualTrue(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Тарифный план с идентификатором " + id + " не найден"));
    }

    private void validatePolicyParams(RatePlanCreateRequest req) {
        switch (req.getPolicyType()) {
            case Unlimited -> {
                if (req.getTimeFrom() != null || req.getTimeTo() != null) {
                    throw new IllegalArgumentException(
                            "Для политики Unlimited заполнены лишние параметры timeFrom, timeTo");
                }
                if (req.getWeeklyLimit() != null) {
                    throw new IllegalArgumentException(
                            "Для политики Unlimited заполнены лишние параметры weeklyLimit");
                }
            }
            case TimeWindow -> {
                if (req.getTimeFrom() == null || req.getTimeTo() == null) {
                    throw new IllegalArgumentException(
                            "Для политики TimeWindow не заполнены обязательные параметры timeFrom, timeTo");
                }
                if (req.getWeeklyLimit() != null) {
                    throw new IllegalArgumentException(
                            "Для политики TimeWindow заполнены лишние параметры weeklyLimit");
                }
            }
            case WeeklyLimit -> {
                if (req.getWeeklyLimit() == null) {
                    throw new IllegalArgumentException(
                            "Для политики WeeklyLimit не заполнены обязательные параметры weeklyLimit");
                }
                if (req.getTimeFrom() != null || req.getTimeTo() != null) {
                    throw new IllegalArgumentException(
                            "Для политики WeeklyLimit заполнены лишние параметры timeFrom, timeTo");
                }
            }
        }
    }

    private void saveOutboxEvent(RatePlan ratePlan, String eventType) {
        try {
            String payload = objectMapper.writeValueAsString(ratePlan);
            OutboxEvent event = new OutboxEvent("RatePlan", ratePlan.getId().toString(), eventType, payload);
            outboxRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Не удалось сериализовать RatePlan", e);
        }
    }
}
