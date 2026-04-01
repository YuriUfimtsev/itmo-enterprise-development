package ru.itmo.ipkn.yuriufimtsev.service.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.ipkn.yuriufimtsev.service.member.dto.AttendanceRequest;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.Attendance;
import ru.itmo.ipkn.yuriufimtsev.service.member.service.AttendanceService;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
@Tag(name = "Посещения", description = "Регистрация посещений с проверкой доступа")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @Operation(summary = "Регистрация посещения", description = "Проверяет абонемент и политику доступа, возвращает решение о допуске")
    public ResponseEntity<EntityModel<Attendance>> registerAttendance(@RequestBody AttendanceRequest request) {
        Attendance attendance = attendanceService.registerAttendance(request.getMemberId());
        return ResponseEntity.ok(EntityModel.of(attendance));
    }
}
