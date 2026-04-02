package ru.itmo.ipkn.yuriufimtsev.service.rateplan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.dto.RatePlanCreateRequest;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.dto.RatePlanUpdatePriceRequest;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.PolicyType;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.model.RatePlan;
import ru.itmo.ipkn.yuriufimtsev.service.rateplan.service.RatePlanService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RatePlanController.class)
class RatePlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RatePlanService ratePlanService;

    @Test
    void getRatePlan_returns200() throws Exception {
        RatePlan plan = new RatePlan();
        plan.setId(1L);
        plan.setPolicyType(PolicyType.Unlimited);
        plan.setDaysDuration(30);
        plan.setPrice(BigDecimal.valueOf(5000));

        when(ratePlanService.getRatePlan(1L)).thenReturn(plan);

        mockMvc.perform(get("/api/rate-plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.policyType").value("Unlimited"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void createRatePlan_returns200() throws Exception {
        RatePlanCreateRequest request = new RatePlanCreateRequest();
        request.setPolicyType(PolicyType.Unlimited);
        request.setDaysDuration(30);
        request.setPrice(BigDecimal.valueOf(5000));

        RatePlan created = new RatePlan();
        created.setId(1L);
        created.setPolicyType(PolicyType.Unlimited);
        created.setDaysDuration(30);
        created.setPrice(BigDecimal.valueOf(5000));

        when(ratePlanService.createRatePlan(any())).thenReturn(created);

        mockMvc.perform(post("/api/rate-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.policyType").value("Unlimited"));
    }

    @Test
    void updatePrice_returns200() throws Exception {
        RatePlanUpdatePriceRequest request = new RatePlanUpdatePriceRequest();
        request.setRatePlanId(1L);
        request.setNewPrice(BigDecimal.valueOf(6000));

        doNothing().when(ratePlanService).updatePrice(1L, BigDecimal.valueOf(6000));

        mockMvc.perform(put("/api/rate-plans/1/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteRatePlan_returns200() throws Exception {
        doNothing().when(ratePlanService).deleteRatePlan(1L);

        mockMvc.perform(delete("/api/rate-plans/1"))
                .andExpect(status().isOk());
    }

    @Test
    void searchRatePlans_returnsList() throws Exception {
        RatePlan plan = new RatePlan();
        plan.setId(1L);
        plan.setPolicyType(PolicyType.Unlimited);
        plan.setDaysDuration(30);
        plan.setPrice(BigDecimal.valueOf(5000));

        when(ratePlanService.searchRatePlans(any())).thenReturn(List.of(plan));

        mockMvc.perform(get("/api/rate-plans"))
                .andExpect(status().isOk());
    }
}
