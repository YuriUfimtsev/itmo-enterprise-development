package ru.itmo.ipkn.yuriufimtsev.service.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.ipkn.yuriufimtsev.service.member.dto.AssignRatePlanRequest;
import ru.itmo.ipkn.yuriufimtsev.service.member.dto.MemberCreateRequest;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.Member;
import ru.itmo.ipkn.yuriufimtsev.service.member.service.MemberService;

import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Посетители", description = "Управление посетителями и абонементами")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/{id}")
    @Operation(summary = "Получение посетителя по Id")
    public ResponseEntity<EntityModel<Member>> getMember(@PathVariable Long id) {
        Member member = memberService.findById(id);
        return ResponseEntity.ok(EntityModel.of(member,
                linkTo(methodOn(MemberController.class).getMember(id)).withSelfRel()));
    }

    @PostMapping
    @Operation(summary = "Регистрация посетителя")
    public ResponseEntity<EntityModel<Map<String, Long>>> registerMember(@RequestBody MemberCreateRequest request) {
        Long id = memberService.registerMember(request.getFirstName(), request.getLastName(),
                request.getBirthDate(), request.getEmail());
        return ResponseEntity.ok(EntityModel.of(Map.of("id", id),
                linkTo(methodOn(MemberController.class).findByEmail(request.getEmail())).withSelfRel()));
    }

    @PostMapping("/assign-rate-plan")
    @Operation(summary = "Назначение тарифного плана посетителю")
    public ResponseEntity<Void> assignRatePlan(@RequestBody AssignRatePlanRequest request) {
        memberService.assignRatePlan(request.getMemberEmail(), request.getRatePlanId());
        return ResponseEntity.ok().build();
    }

    @GetMapping(params = "email")
    @Operation(summary = "Поиск посетителя по email")
    public ResponseEntity<EntityModel<Member>> findByEmail(@RequestParam String email) {
        Member member = memberService.findByEmail(email);
        return ResponseEntity.ok(EntityModel.of(member,
                linkTo(methodOn(MemberController.class).findByEmail(email)).withSelfRel()));
    }
}
