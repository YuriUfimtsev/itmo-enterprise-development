package ru.itmo.ipkn.yuriufimtsev.service.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.ipkn.yuriufimtsev.service.auth.service.AuthService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Регистрация и аутентификация администраторов")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового администратора")
    public ResponseEntity<Void> registerAdmin(@RequestParam String firstName,
                                              @RequestParam String lastName,
                                              @RequestParam LocalDate birthDate,
                                              @RequestParam String email,
                                              @RequestParam String password) {
        authService.registerAdmin(firstName, lastName, birthDate, email, password);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    @Operation(summary = "Аутентификация администратора", description = "Возвращает JWT-токен")
    public ResponseEntity<Map<String, String>> authenticate(@RequestParam String email,
                                                            @RequestParam String password) {
        String token = authService.authenticate(email, password);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/confirm")
    @Operation(summary = "Подтверждение регистрации администратора")
    public ResponseEntity<Void> confirmAdmin(@RequestParam String email) {
        authService.confirmAdmin(email);
        return ResponseEntity.ok().build();
    }
}
