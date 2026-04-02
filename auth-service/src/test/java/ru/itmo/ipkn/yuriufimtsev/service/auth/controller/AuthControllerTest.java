package ru.itmo.ipkn.yuriufimtsev.service.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.itmo.ipkn.yuriufimtsev.service.auth.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ivan","lastName":"Ivanov","birthDate":"1990-01-01","email":"admin@test.com","password":"secret"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void authenticate_returnsToken() throws Exception {
        when(authService.authenticate("admin@test.com", "secret")).thenReturn("jwt-token-123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.com","password":"secret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"));
    }

    @Test
    void confirmAdmin_returns200() throws Exception {
        doNothing().when(authService).confirmAdmin("admin@test.com");

        mockMvc.perform(post("/api/auth/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.com"}
                                """))
                .andExpect(status().isOk());
    }
}
