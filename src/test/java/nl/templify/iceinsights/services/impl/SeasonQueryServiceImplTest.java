package nl.templify.iceinsights.services.impl;

import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.domain.Chip;
import nl.templify.iceinsights.domain.Season;
import nl.templify.iceinsights.domain.User;
import nl.templify.iceinsights.dto.SeasonSummaryDto;
import nl.templify.iceinsights.exceptions.SeasonNotFoundException;
import nl.templify.iceinsights.repositories.ActivityRepository;
import nl.templify.iceinsights.repositories.SeasonRepository;
import nl.templify.iceinsights.repositories.UserRepository;
import nl.templify.iceinsights.services.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeasonQueryServiceImplTest {

    @Mock private ActivityRepository activityRepository;
    @Mock private SeasonRepository seasonRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuthenticationFacade authFacade;

    private SeasonQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SeasonQueryServiceImpl(
                activityRepository, seasonRepository, userRepository, authFacade);
        lenient().when(authFacade.getCurrentUsername()).thenReturn("barry");
        lenient().when(userRepository.findByUsername("barry")).thenReturn(Optional.of(userWithChip(7L)));
    }

    @Test
    void minAggregation_twoActivities_missingBest13UsesTheOther() {
        Season season = season(5L, "2025/2026");
        Activity first = activity(1L, 7L, 5L, 2822L);
        first.setBest1Duration("50.000");
        first.setBest13Duration(null);
        Activity second = activity(2L, 7L, 5L, 2822L);
        second.setBest1Duration("49.000");
        second.setBest13Duration("10:50.000");

        when(seasonRepository.findById(5L)).thenReturn(Optional.of(season));
        when(activityRepository.findByChipIdInAndSeasonId(List.of(7L), 5L))
                .thenReturn(List.of(first, second));

        SeasonSummaryDto dto = service.getCurrentUserSeason(5L, null);

        assertEquals(5L, dto.getId());
        assertEquals("2025/2026", dto.getLabel());
        assertEquals(LocalDate.of(2025, 5, 1), dto.getStartDate());
        assertEquals(LocalDate.of(2026, 4, 30), dto.getEndDate());
        assertEquals("49.000", dto.getBest1Duration());
        assertEquals("10:50.000", dto.getBest13Duration());
    }

    @Test
    void getCurrentUserSeason_missingOrNoActivities_throwsNotFound() {
        when(seasonRepository.findById(99L)).thenReturn(Optional.empty());

        SeasonNotFoundException missing = assertThrows(
                SeasonNotFoundException.class,
                () -> service.getCurrentUserSeason(99L, null));
        assertEquals("Season not found", missing.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, missing.getClass().getAnnotation(ResponseStatus.class).value());

        Season season = season(5L, "2025/2026");
        when(seasonRepository.findById(5L)).thenReturn(Optional.of(season));
        when(activityRepository.findByChipIdInAndSeasonId(List.of(7L), 5L)).thenReturn(List.of());

        SeasonNotFoundException empty = assertThrows(
                SeasonNotFoundException.class,
                () -> service.getCurrentUserSeason(5L, null));
        assertEquals("Season not found", empty.getMessage());
    }

    @Test
    void listCurrentUserSeasons_newestStartDateFirst_andLocationFilter() {
        Season newer = season(2L, "2026/2027");
        newer.setStartDate(LocalDate.of(2026, 5, 1));
        newer.setEndDate(LocalDate.of(2027, 4, 30));
        Season older = season(1L, "2025/2026");
        when(activityRepository.findDistinctSeasonIdsByChipIdIn(List.of(7L)))
                .thenReturn(List.of(1L, 2L));
        when(seasonRepository.findByIdInOrderByStartDateDesc(List.of(1L, 2L)))
                .thenReturn(List.of(newer, older));
        when(activityRepository.findByChipIdInAndSeasonId(List.of(7L), 2L))
                .thenReturn(List.of(activity(10L, 7L, 2L, 2822L)));
        when(activityRepository.findByChipIdInAndSeasonId(List.of(7L), 1L))
                .thenReturn(List.of(activity(11L, 7L, 1L, 2822L)));

        List<SeasonSummaryDto> all = service.listCurrentUserSeasons(null);
        assertEquals(2, all.size());
        assertEquals(2L, all.get(0).getId());
        assertEquals(1L, all.get(1).getId());

        when(activityRepository.findDistinctSeasonIdsByChipIdInAndLocationId(List.of(7L), 99L))
                .thenReturn(List.of());
        List<SeasonSummaryDto> filtered = service.listCurrentUserSeasons(99L);
        assertEquals(List.of(), filtered);
        verify(activityRepository, never()).findByChipIdInAndSeasonIdAndLocationId(any(), any(), eq(99L));
    }

    @Test
    void minDuration_skipsNullAndKeepsFasterStoredString() {
        Activity slow = activity(1L, 7L, 5L, 1L);
        slow.setBest4Duration("3:20.000");
        Activity missing = activity(2L, 7L, 5L, 1L);
        missing.setBest4Duration(null);
        Activity fast = activity(3L, 7L, 5L, 1L);
        fast.setBest4Duration("3:16.000");

        assertEquals("3:16.000",
                SeasonQueryServiceImpl.minDuration(List.of(slow, missing, fast), Activity::getBest4Duration));
        assertNull(SeasonQueryServiceImpl.minDuration(List.of(missing), Activity::getBest13Duration));
    }

    private static User userWithChip(Long chipId) {
        Chip chip = Chip.builder().id(chipId).chipCode("PW-1").build();
        return User.builder()
                .id(1L)
                .username("barry")
                .chips(new HashSet<>(Set.of(chip)))
                .build();
    }

    private static Season season(Long id, String label) {
        return Season.builder()
                .id(id)
                .label(label)
                .startDate(LocalDate.of(2025, 5, 1))
                .endDate(LocalDate.of(2026, 4, 30))
                .build();
    }

    private static Activity activity(Long id, Long chipId, Long seasonId, Long locationId) {
        return Activity.builder()
                .id(id)
                .name("Practice")
                .chipId(chipId)
                .seasonId(seasonId)
                .locationId(locationId)
                .build();
    }
}
