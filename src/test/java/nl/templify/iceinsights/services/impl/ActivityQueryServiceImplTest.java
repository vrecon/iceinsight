package nl.templify.iceinsights.services.impl;

import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.domain.Chip;
import nl.templify.iceinsights.domain.Session;
import nl.templify.iceinsights.domain.SessionStats;
import nl.templify.iceinsights.domain.User;
import nl.templify.iceinsights.dto.ActivitySummaryDto;
import nl.templify.iceinsights.exceptions.ActivityNotFoundException;
import nl.templify.iceinsights.mapper.ActivitySummaryMapper;
import nl.templify.iceinsights.repositories.ActivityRepository;
import nl.templify.iceinsights.repositories.SessionRepository;
import nl.templify.iceinsights.repositories.UserRepository;
import nl.templify.iceinsights.services.AuthenticationFacade;
import nl.templify.iceinsights.services.SessionAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityQueryServiceImplTest {

    @Mock private ActivityRepository activityRepository;
    @Mock private SessionRepository sessionRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuthenticationFacade authFacade;
    @Mock private SessionAnalyticsService sessionAnalyticsService;

    private ActivityQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ActivityQueryServiceImpl(
                activityRepository,
                sessionRepository,
                userRepository,
                authFacade,
                sessionAnalyticsService,
                new ActivitySummaryMapper());
        when(authFacade.getCurrentUsername()).thenReturn("barry");
        when(userRepository.findByUsername("barry")).thenReturn(Optional.of(userWithChip(7L)));
    }

    @Test
    void getCurrentUserActivity_missing_throwsNotFound() {
        when(activityRepository.findById(99L)).thenReturn(Optional.empty());

        ActivityNotFoundException ex = assertThrows(
                ActivityNotFoundException.class,
                () -> service.getCurrentUserActivity(99L));
        assertEquals("Activity not found", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getClass().getAnnotation(ResponseStatus.class).value());
        verify(sessionAnalyticsService, never()).applyActivityBests(any(), any());
    }

    @Test
    void getCurrentUserActivity_otherUsersChip_throwsNotFound() {
        Activity foreign = activity(42L, 99L, "51.000");
        when(activityRepository.findById(42L)).thenReturn(Optional.of(foreign));

        ActivityNotFoundException ex = assertThrows(
                ActivityNotFoundException.class,
                () -> service.getCurrentUserActivity(42L));
        assertEquals("Activity not found", ex.getMessage());
        verify(sessionRepository, never()).findByActivityIdWithStats(any());
    }

    @Test
    void getCurrentUserActivity_returnsStoredBestsForOwnChip() {
        Activity own = activity(10L, 7L, "49.000");
        own.setBest4Duration("3:16.000");
        own.setBest8Duration("6:32.000");
        own.setBest13Duration("10:50.000");
        when(activityRepository.findById(10L)).thenReturn(Optional.of(own));

        ActivitySummaryDto dto = service.getCurrentUserActivity(10L);

        assertEquals(10L, dto.getId());
        assertEquals(7L, dto.getChipId());
        assertEquals("49.000", dto.getBest1Duration());
        assertEquals("3:16.000", dto.getBest4Duration());
        assertEquals("6:32.000", dto.getBest8Duration());
        assertEquals("10:50.000", dto.getBest13Duration());
        verify(sessionAnalyticsService, never()).applyActivityBests(any(), any());
    }

    @Test
    void getCurrentUserActivity_nullBests_backfillsFromSessionStats() {
        Activity own = activity(10L, 7L, null);
        when(activityRepository.findById(10L)).thenReturn(Optional.of(own));
        Session session = Session.builder()
                .stats(SessionStats.builder().best1Duration("48.000").best13Duration(null).build())
                .build();
        when(sessionRepository.findByActivityIdWithStats(10L)).thenReturn(List.of(session));
        when(activityRepository.save(own)).thenReturn(own);

        org.mockito.Mockito.doAnswer(invocation -> {
            Activity activity = invocation.getArgument(0);
            activity.setBest1Duration("48.000");
            activity.setBest13Duration(null);
            return null;
        }).when(sessionAnalyticsService).applyActivityBests(eq(own), eq(List.of(session)));

        ActivitySummaryDto dto = service.getCurrentUserActivity(10L);

        assertEquals("48.000", dto.getBest1Duration());
        assertEquals(null, dto.getBest13Duration());
        verify(sessionAnalyticsService).applyActivityBests(own, List.of(session));
        verify(activityRepository).save(own);
    }

    @Test
    void listCurrentUserActivities_returnsNewestFirstFromRepository() {
        Activity newer = activity(2L, 7L, "49.000");
        newer.setStartTime(ZonedDateTime.parse("2026-08-20T10:00:00+02:00"));
        Activity older = activity(1L, 7L, "50.000");
        older.setStartTime(ZonedDateTime.parse("2026-08-19T10:00:00+02:00"));
        when(activityRepository.findByChipIdInOrderByStartTimeDesc(List.of(7L)))
                .thenReturn(List.of(newer, older));

        List<ActivitySummaryDto> result = service.listCurrentUserActivities();

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals(1L, result.get(1).getId());
        ArgumentCaptor<java.util.Collection<Long>> captor = ArgumentCaptor.forClass(java.util.Collection.class);
        verify(activityRepository).findByChipIdInOrderByStartTimeDesc(captor.capture());
        assertEquals(List.of(7L), List.copyOf(captor.getValue()));
    }

    private static User userWithChip(Long chipId) {
        Chip chip = Chip.builder().id(chipId).chipCode("PW-1").build();
        return User.builder()
                .id(1L)
                .username("barry")
                .chips(new HashSet<>(Set.of(chip)))
                .build();
    }

    private static Activity activity(Long id, Long chipId, String best1) {
        return Activity.builder()
                .id(id)
                .name("Practice")
                .chipId(chipId)
                .startTime(ZonedDateTime.parse("2026-08-20T09:00:00+02:00"))
                .best1Duration(best1)
                .build();
    }
}
