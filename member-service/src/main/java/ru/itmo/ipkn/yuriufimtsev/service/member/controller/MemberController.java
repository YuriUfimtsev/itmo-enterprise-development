package ru.itmo.ipkn.yuriufimtsev.service.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.Member;
import ru.itmo.ipkn.yuriufimtsev.service.member.service.MemberService;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Посетители", description = "Управление посетителями и абонементами")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @Operation(summary = "Регистрация посетителя")
    public ResponseEntity<EntityModel<Map<String, Long>>> registerMember(@RequestParam String firstName,
                                                                         @RequestParam String lastName,
                                                                         @RequestParam LocalDate birthDate,
                                                                         @RequestParam String email) {
        Long id = memberService.registerMember(firstName, lastName, birthDate, email);
        return ResponseEntity.ok(EntityModel.of(Map.of("id", id),
                linkTo(methodOn(MemberController.class).findByEmail(email)).withSelfRel()));
    }

    @PostMapping("/assign-rate-plan")
    @Operation(summary = "Назначение тарифного плана посетителю")
    public ResponseEntity<Void> assignRatePlan(@RequestParam String memberEmail,
                                               @RequestParam Long ratePlanId) {
        memberService.assignRatePlan(memberEmail, ratePlanId);
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
