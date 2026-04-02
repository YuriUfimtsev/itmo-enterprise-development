package ru.itmo.ipkn.yuriufimtsev.service.member.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.Member;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.RatePlanSnapshot;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.Role;
import ru.itmo.ipkn.yuriufimtsev.service.member.model.Subscription;
import ru.itmo.ipkn.yuriufimtsev.service.member.repository.MemberRepository;
import ru.itmo.ipkn.yuriufimtsev.service.member.repository.RatePlanSnapshotRepository;
import ru.itmo.ipkn.yuriufimtsev.service.member.repository.SubscriptionRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private RatePlanSnapshotRepository ratePlanSnapshotRepository;

    @InjectMocks
    private MemberService memberService;

    private static final String EMAIL = "user@test.com";
    private static final LocalDate BIRTH_DATE = LocalDate.of(2000, 1, 1);

    private Member.MemberBuilder defaultMember() {
        return Member.builder()
                .firstName("Ivan")
                .lastName("Ivanov")
                .birthDate(BIRTH_DATE)
                .email(EMAIL)
                .role(Role.MEMBER);
    }

    @Test
    void registerMember_validData_returnsId() {
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        var saved = defaultMember().id(1L).build();
        when(memberRepository.save(any(Member.class))).thenReturn(saved);

        Long id = memberService.registerMember("Ivan", "Ivanov", BIRTH_DATE, EMAIL);

        assertEquals(1L, id);
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void registerMember_duplicateEmail_throwsException() {
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(defaultMember().build()));

        var ex = assertThrows(IllegalArgumentException.class, () ->
                memberService.registerMember("A", "B", BIRTH_DATE, EMAIL));

        assertTrue(ex.getMessage().contains("уже зарегистрирован"));
    }

    @Test
    void registerMember_adminEmail_throwsException() {
        var admin = defaultMember().role(Role.ADMIN).email("admin@test.com").build();
        when(memberRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        var ex = assertThrows(IllegalArgumentException.class, () ->
                memberService.registerMember("A", "B", BIRTH_DATE, "admin@test.com"));

        assertTrue(ex.getMessage().contains("администратором"));
    }

    @Test
    void assignRatePlan_noExistingSubscription_createsNew() {
        var member = defaultMember().id(1L).build();
        var ratePlan = RatePlanSnapshot.builder()
                .id(10L)
                .daysDuration(30)
                .isActual(true)
                .build();

        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(member));
        when(ratePlanSnapshotRepository.findById(10L)).thenReturn(Optional.of(ratePlan));
        when(subscriptionRepository.findActiveByMemberId(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        memberService.assignRatePlan(EMAIL, 10L);

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void assignRatePlan_existingSubscription_updatesIt() {
        var member = defaultMember().id(1L).build();
        var ratePlan = RatePlanSnapshot.builder()
                .id(20L)
                .daysDuration(60)
                .isActual(true)
                .build();
        var existingSub = Subscription.builder()
                .id(5L)
                .memberId(1L)
                .ratePlanId(10L)
                .from(LocalDate.now().minusDays(10))
                .to(LocalDate.now().plusDays(20))
                .build();

        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(member));
        when(ratePlanSnapshotRepository.findById(20L)).thenReturn(Optional.of(ratePlan));
        when(subscriptionRepository.findActiveByMemberId(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(existingSub));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        memberService.assignRatePlan(EMAIL, 20L);

        assertEquals(20L, existingSub.getRatePlanId());
        assertEquals(LocalDate.now().plusDays(60), existingSub.getTo());
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void assignRatePlan_memberNotFound_throwsException() {
        when(memberRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                memberService.assignRatePlan("nobody@test.com", 1L));
    }

    @Test
    void assignRatePlan_ratePlanNotFound_throwsException() {
        var member = defaultMember().id(1L).build();
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(member));
        when(ratePlanSnapshotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                memberService.assignRatePlan(EMAIL, 99L));
    }

    @Test
    void findByEmail_existingMember_returnsMember() {
        var member = defaultMember().build();
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(member));

        Member found = memberService.findByEmail(EMAIL);

        assertEquals("Ivan", found.getFirstName());
    }

    @Test
    void findByEmail_notFound_throwsException() {
        when(memberRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                memberService.findByEmail("nobody@test.com"));
    }
}
