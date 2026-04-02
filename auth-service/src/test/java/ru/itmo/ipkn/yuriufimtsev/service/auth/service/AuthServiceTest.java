package ru.itmo.ipkn.yuriufimtsev.service.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.itmo.ipkn.yuriufimtsev.service.auth.model.Admin;
import ru.itmo.ipkn.yuriufimtsev.service.auth.model.AdminStatus;
import ru.itmo.ipkn.yuriufimtsev.service.auth.repository.AdminRepository;
import ru.itmo.ipkn.yuriufimtsev.service.auth.security.JwtProvider;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    private static final String EMAIL = "admin@test.com";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1990, 1, 1);

    private Admin.AdminBuilder defaultAdmin() {
        return Admin.builder()
                .firstName("A")
                .lastName("B")
                .birthDate(BIRTH_DATE)
                .email(EMAIL)
                .passwordHash("hash")
                .status(AdminStatus.PENDING);
    }

    @Test
    void registerAdmin_validData_savesAdmin() {
        when(adminRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(adminRepository.save(any(Admin.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() ->
                authService.registerAdmin("Ivan", "Ivanov", BIRTH_DATE, EMAIL, "pass123"));

        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void registerAdmin_duplicateEmail_throwsException() {
        when(adminRepository.findByEmail(EMAIL)).thenReturn(Optional.of(defaultAdmin().build()));

        var ex = assertThrows(IllegalArgumentException.class, () ->
                authService.registerAdmin("Ivan", "Ivanov", BIRTH_DATE, EMAIL, "pass123"));

        assertTrue(ex.getMessage().contains("уже используется"));
        verify(adminRepository, never()).save(any());
    }

    @Test
    void authenticate_validCredentials_returnsToken() {
        var encoder = new BCryptPasswordEncoder();
        var admin = defaultAdmin()
                .status(AdminStatus.ACTIVE)
                .passwordHash(encoder.encode("pass123"))
                .build();
        when(adminRepository.findByEmail(EMAIL)).thenReturn(Optional.of(admin));
        when(jwtProvider.generateToken(admin)).thenReturn("jwt-token-123");

        String token = authService.authenticate(EMAIL, "pass123");

        assertEquals("jwt-token-123", token);
        verify(jwtProvider).generateToken(admin);
    }

    @Test
    void authenticate_pendingStatus_throwsException() {
        var admin = defaultAdmin().status(AdminStatus.PENDING).build();
        when(adminRepository.findByEmail(EMAIL)).thenReturn(Optional.of(admin));

        var ex = assertThrows(IllegalArgumentException.class, () ->
                authService.authenticate(EMAIL, "pass123"));

        assertTrue(ex.getMessage().contains("Аккаунт ожидает подтверждения"));
    }

    @Test
    void authenticate_wrongPassword_throwsException() {
        var encoder = new BCryptPasswordEncoder();
        var admin = defaultAdmin()
                .status(AdminStatus.ACTIVE)
                .passwordHash(encoder.encode("correctPassword"))
                .build();
        when(adminRepository.findByEmail(EMAIL)).thenReturn(Optional.of(admin));

        var ex = assertThrows(IllegalArgumentException.class, () ->
                authService.authenticate(EMAIL, "wrongPassword"));

        assertTrue(ex.getMessage().contains("Неверный email или пароль"));
    }

    @Test
    void authenticate_wrongEmail_throwsException() {
        when(adminRepository.findByEmail("wrong@test.com")).thenReturn(Optional.empty());

        var ex = assertThrows(IllegalArgumentException.class, () ->
                authService.authenticate("wrong@test.com", "pass123"));

        assertTrue(ex.getMessage().contains("Неверный email или пароль"));
    }

    @Test
    void confirmAdmin_existingAdmin_setsStatusActive() {
        var admin = defaultAdmin().status(AdminStatus.PENDING).build();
        when(adminRepository.findByEmail(EMAIL)).thenReturn(Optional.of(admin));
        when(adminRepository.save(any(Admin.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.confirmAdmin(EMAIL);

        assertEquals(AdminStatus.ACTIVE, admin.getStatus());
        verify(adminRepository).save(admin);
    }

    @Test
    void confirmAdmin_notFound_throwsException() {
        when(adminRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                authService.confirmAdmin("unknown@test.com"));
    }
}
