package ru.itmo.ipkn.yuriufimtsev.service.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRatePlanRequest {
    private String memberEmail;
    private Long ratePlanId;
}
