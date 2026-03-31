package ru.itmo.ipkn.yuriufimtsev.service.member.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.Attendance;
import ru.itmo.ipkn.yuriufimtsev.service.member.service.AttendanceService;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttendanceController.class)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceService attendanceService;

    @Test
    void registerAttendance_allowed() throws Exception {
        Attendance attendance = new Attendance(1L, LocalDateTime.of(2025, 3, 25, 10, 0), true, "");
        attendance.setId(1L);
        when(attendanceService.registerAttendance(1L)).thenReturn(attendance);

        mockMvc.perform(post("/api/attendances")
                        .param("memberId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(true));
    }

    @Test
    void registerAttendance_denied() throws Exception {
        Attendance attendance = new Attendance(1L, LocalDateTime.of(2025, 3, 25, 10, 0),
                false, "Не найден актуальный абонемент посетителя");
        attendance.setId(2L);
        when(attendanceService.registerAttendance(1L)).thenReturn(attendance);

        mockMvc.perform(post("/api/attendances")
                        .param("memberId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.reason").value("Не найден актуальный абонемент посетителя"));
    }
}
