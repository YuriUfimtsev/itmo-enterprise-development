package ru.itmo.ipkn.yuriufimtsev.service.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberCreateRequest {
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String email;
}
