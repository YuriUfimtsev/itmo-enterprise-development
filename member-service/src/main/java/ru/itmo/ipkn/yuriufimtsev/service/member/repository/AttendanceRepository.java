package ru.itmo.ipkn.yuriufimtsev.service.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.Attendance;

import java.time.LocalDateTime;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("""
            SELECT COUNT(a) FROM Attendance a
            WHERE a.memberId = :memberId AND a.allowed = true
            AND a.timestamp >= :since
            """)
    int countAllowedVisitsLast7Days(Long memberId, LocalDateTime since);
}
