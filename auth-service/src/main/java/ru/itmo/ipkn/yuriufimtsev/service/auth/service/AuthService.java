package ru.itmo.ipkn.yuriufimtsev.service.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ru.itmo.ipkn.yuriufimtsev.service.auth.model.Admin;
import ru.itmo.ipkn.yuriufimtsev.service.auth.model.AdminStatus;
import ru.itmo.ipkn.yuriufimtsev.service.auth.repository.AdminRepository;
import ru.itmo.ipkn.yuriufimtsev.service.auth.security.JwtProvider;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminRepository adminRepository;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void registerAdmin(String firstName, String lastName, LocalDate birthDate,
                              String email, String rawPassword) {
        if (adminRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                    "Email-адрес уже используется");
        }

        String hash = passwordEncoder.encode(rawPassword);
        Admin admin = new Admin(firstName, lastName, birthDate, email, hash);
        adminRepository.save(admin);
    }

    public String authenticate(String email, String rawPassword) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Неверный email или пароль"));

        if (admin.getStatus() != AdminStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Аккаунт ожидает подтверждения");
        }

        if (!passwordEncoder.matches(rawPassword, admin.getPasswordHash())) {
            throw new IllegalArgumentException(
                    "Неверный email или пароль");
        }

        return jwtProvider.generateToken(admin);
    }

    public void confirmAdmin(String email) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Администратор с почтой " + email + " не найден"));

        admin.setStatus(AdminStatus.ACTIVE);
        adminRepository.save(admin);
    }
}
