package ru.itmo.ipkn.yuriufimtsev.service.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.ipkn.yuriufimtsev.service.auth.dto.ConfirmRequest;
import ru.itmo.ipkn.yuriufimtsev.service.auth.dto.LoginRequest;
import ru.itmo.ipkn.yuriufimtsev.service.auth.dto.RegisterRequest;
import ru.itmo.ipkn.yuriufimtsev.service.auth.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Регистрация и аутентификация администраторов")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового администратора")
    public ResponseEntity<Void> registerAdmin(@RequestBody RegisterRequest request) {
        authService.registerAdmin(request.getFirstName(), request.getLastName(),
                request.getBirthDate(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    @Operation(summary = "Аутентификация администратора", description = "Возвращает JWT-токен")
    public ResponseEntity<Map<String, String>> authenticate(@RequestBody LoginRequest request) {
        String token = authService.authenticate(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/confirm")
    @Operation(summary = "Подтверждение регистрации администратора")
    public ResponseEntity<Void> confirmAdmin(@RequestBody ConfirmRequest request) {
        authService.confirmAdmin(request.getEmail());
        return ResponseEntity.ok().build();
    }
}
