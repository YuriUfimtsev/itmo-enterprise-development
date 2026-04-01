package ru.itmo.ipkn.yuriufimtsev.service.rateplan.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.dto.RatePlanCreateRequest;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.dto.RatePlanSearchRequest;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.dto.RatePlanUpdatePriceRequest;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.RatePlan;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.service.RatePlanService;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/rate-plans")
@RequiredArgsConstructor
@Tag(name = "Тарифные планы", description = "CRUD-операции над тарифными планами")
public class RatePlanController {

    private final RatePlanService ratePlanService;

    @PostMapping
    @Operation(summary = "Создание тарифного плана")
    public ResponseEntity<EntityModel<RatePlan>> createRatePlan(@RequestBody RatePlanCreateRequest request) {
        RatePlan created = ratePlanService.createRatePlan(request);
        return ResponseEntity.ok(toModel(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение тарифного плана по Id")
    public ResponseEntity<EntityModel<RatePlan>> getRatePlan(@PathVariable Long id) {
        RatePlan ratePlan = ratePlanService.getRatePlan(id);
        return ResponseEntity.ok(toModel(ratePlan));
    }

    @PutMapping("/{id}/price")
    @Operation(summary = "Обновление цены тарифного плана")
    public ResponseEntity<Void> updatePrice(@PathVariable Long id,
                                            @RequestBody RatePlanUpdatePriceRequest request) {
        ratePlanService.updatePrice(id, request.getNewPrice());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление (деактивация) тарифного плана")
    public ResponseEntity<Void> deleteRatePlan(@PathVariable Long id) {
        ratePlanService.deleteRatePlan(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Поиск тарифных планов", description = "Фильтрация по политике и параметрам. Без параметров возвращает все активные планы")
    public ResponseEntity<CollectionModel<EntityModel<RatePlan>>> searchRatePlans(RatePlanSearchRequest request) {
        List<EntityModel<RatePlan>> models = ratePlanService.searchRatePlans(request).stream()
                .map(this::toModel)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(models,
                linkTo(methodOn(RatePlanController.class).searchRatePlans(request)).withSelfRel()));
    }

    private EntityModel<RatePlan> toModel(RatePlan ratePlan) {
        return EntityModel.of(ratePlan,
                linkTo(methodOn(RatePlanController.class).getRatePlan(ratePlan.getId())).withSelfRel(),
                linkTo(methodOn(RatePlanController.class).searchRatePlans(null)).withRel("rate-plans"),
                linkTo(methodOn(RatePlanController.class).deleteRatePlan(ratePlan.getId())).withRel("delete"));
    }
}
