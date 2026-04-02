package ru.itmo.ipkn.yuriufimtsev.service.member.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.Member;
import ru.itmo.ipkn.yuriufimtsev.service.member.service.MemberService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Test
    void getMember_returns200() throws Exception {
        Member member = new Member("Ivan", "Ivanov", LocalDate.of(2000, 1, 1), "user@test.com");
        member.setId(1L);
        when(memberService.findById(1L)).thenReturn(member);

        mockMvc.perform(get("/api/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ivan"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void registerMember_returns200() throws Exception {
        when(memberService.registerMember(any(), any(), any(), any())).thenReturn(1L);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ivan","lastName":"Ivanov","birthDate":"2000-01-01","email":"user@test.com"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void findByEmail_returnsMember() throws Exception {
        Member member = new Member("Ivan", "Ivanov", LocalDate.of(2000, 1, 1), "user@test.com");
        member.setId(1L);
        when(memberService.findByEmail("user@test.com")).thenReturn(member);

        mockMvc.perform(get("/api/members")
                        .param("email", "user@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ivan"));
    }

    @Test
    void assignRatePlan_returns200() throws Exception {
        doNothing().when(memberService).assignRatePlan("user@test.com", 1L);

        mockMvc.perform(post("/api/members/assign-rate-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memberEmail":"user@test.com","ratePlanId":1}
                                """))
                .andExpect(status().isOk());
    }
}
