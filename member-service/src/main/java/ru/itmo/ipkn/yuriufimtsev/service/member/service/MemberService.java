package ru.itmo.ipkn.yuriufimtsev.service.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.Member;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.RatePlanSnapshot;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.Role;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.Subscription;
import ru.itmo.ipkn.yuriufimtsev.service.member.repository.MemberRepository;
import ru.itmo.ipkn.yuriufimtsev.service.member.repository.RatePlanSnapshotRepository;
import ru.itmo.ipkn.yuriufimtsev.service.member.repository.SubscriptionRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RatePlanSnapshotRepository ratePlanSnapshotRepository;


    // Зачем Transactional в начале метода?
    @Transactional
    public Long registerMember(String firstName, String lastName, LocalDate birthDate, String email) {
        memberRepository.findByEmail(email).ifPresent(existing -> {
            if (existing.getRole() == Role.ADMIN) {
                throw new IllegalArgumentException(
                        "Данный email-адрес используется администратором системы");
            }
            throw new IllegalArgumentException(
                    "Посетитель " + existing.getFirstName() + " " + existing.getLastName()
                            + " " + existing.getBirthDate().getYear()
                            + " года рождения уже зарегистрирован по данному email-адресу");
        });

        Member member = new Member(firstName, lastName, birthDate, email);
        return memberRepository.save(member).getId();
    }

    @Transactional
    public void assignRatePlan(String memberEmail, Long ratePlanId) {
        Member member = memberRepository.findByEmail(memberEmail)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Посетитель с почтой " + memberEmail + " не найден"));

        RatePlanSnapshot ratePlan = ratePlanSnapshotRepository.findById(ratePlanId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Тарифный план с идентификатором " + ratePlanId + " не найден"));

        LocalDate today = LocalDate.now();
        Subscription subscription = subscriptionRepository
                .findActiveByMemberId(member.getId(), today)
                .orElse(new Subscription());

        subscription.setMemberId(member.getId());
        subscription.setRatePlanId(ratePlanId);
        subscription.setFrom(today);
        subscription.setTo(today.plusDays(ratePlan.getDaysDuration()));
        subscriptionRepository.save(subscription);
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Пользователь с почтой " + email + " не найден"));
    }
}
