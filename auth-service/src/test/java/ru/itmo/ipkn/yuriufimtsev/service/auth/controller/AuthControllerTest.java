package ru.itmo.ipkn.yuriufimtsev.service.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.itmo.ipkn.yuriufimtsev.service.auth.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void registerAdmin_returns200() throws Exception {
        doNothing().when(authService).registerAdmin(any(), any(), any(), any(), any());

        mockMvc.perform(post("/api/auth/register")
                        .param("firstName", "Ivan")
                        .param("lastName", "Ivanov")
                        .param("birthDate", "1990-01-01")
                        .param("email", "admin@test.com")
                        .param("password", "secret"))
                .andExpect(status().isOk());
    }

    @Test
    void authenticate_returnsToken() throws Exception {
        when(authService.authenticate("admin@test.com", "secret")).thenReturn("jwt-token-123");

        mockMvc.perform(post("/api/auth/login")
                        .param("email", "admin@test.com")
                        .param("password", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"));
    }

    @Test
    void confirmAdmin_returns200() throws Exception {
        doNothing().when(authService).confirmAdmin("admin@test.com");

        mockMvc.perform(post("/api/auth/confirm")
                        .param("email", "admin@test.com"))
                .andExpect(status().isOk());
    }
}
