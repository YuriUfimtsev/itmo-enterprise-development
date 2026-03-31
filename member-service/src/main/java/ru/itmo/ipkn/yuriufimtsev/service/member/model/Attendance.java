package ru.itmo.ipkn.yuriufimtsev.service.member.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendances")
@Getter
@Setter
@NoArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private boolean allowed;

    private String reason;

    public Attendance(Long memberId, LocalDateTime timestamp, boolean allowed, String reason) {
        this.memberId = memberId;
        this.timestamp = timestamp;
        this.allowed = allowed;
        this.reason = reason;
    }
}
