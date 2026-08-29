package nl.templify.iceinsights.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
import nl.templify.iceinsights.services.SeasonQueryService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SeasonQueryServiceImpl implements SeasonQueryService {

    private final ActivityRepository activityRepository;
    private final SeasonRepository seasonRepository;
    private final UserRepository userRepository;
    private final AuthenticationFacade authFacade;

    @Override
    @Transactional
    public List<SeasonSummaryDto> listCurrentUserSeasons(Long locationId) {
        List<Long> chipIds = currentUserChipIds();
        if (chipIds.isEmpty()) {
            return List.of();
        }
        List<Long> seasonIds = locationId == null
                ? activityRepository.findDistinctSeasonIdsByChipIdIn(chipIds)
                : activityRepository.findDistinctSeasonIdsByChipIdInAndLocationId(chipIds, locationId);
        if (seasonIds.isEmpty()) {
            return List.of();
        }
        return seasonRepository.findByIdInOrderByStartDateDesc(seasonIds).stream()
                .map(season -> toSummary(season, activitiesFor(chipIds, season.getId(), locationId)))
                .toList();
    }

    @Override
    @Transactional
    public SeasonSummaryDto getCurrentUserSeason(Long id, Long locationId) {
        List<Long> chipIds = currentUserChipIds();
        Season season = seasonRepository.findById(id)
                .orElseThrow(() -> new SeasonNotFoundException("Season not found"));
        List<Activity> activities = activitiesFor(chipIds, id, locationId);
        if (activities.isEmpty()) {
            throw new SeasonNotFoundException("Season not found");
        }
        return toSummary(season, activities);
    }

    private List<Activity> activitiesFor(Collection<Long> chipIds, Long seasonId, Long locationId) {
        if (chipIds.isEmpty()) {
            return List.of();
        }
        if (locationId == null) {
            return activityRepository.findByChipIdInAndSeasonId(chipIds, seasonId);
        }
        return activityRepository.findByChipIdInAndSeasonIdAndLocationId(chipIds, seasonId, locationId);
    }

    private List<Long> currentUserChipIds() {
        String username = authFacade.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user.getChips() == null) {
            return List.of();
        }
        return user.getChips().stream().map(Chip::getId).toList();
    }

    static SeasonSummaryDto toSummary(Season season, List<Activity> activities) {
        return SeasonSummaryDto.builder()
                .id(season.getId())
                .label(season.getLabel())
                .startDate(season.getStartDate())
                .endDate(season.getEndDate())
                .best1Duration(minDuration(activities, Activity::getBest1Duration))
                .best2Duration(minDuration(activities, Activity::getBest2Duration))
                .best4Duration(minDuration(activities, Activity::getBest4Duration))
                .best8Duration(minDuration(activities, Activity::getBest8Duration))
                .best13Duration(minDuration(activities, Activity::getBest13Duration))
                .best25Duration(minDuration(activities, Activity::getBest25Duration))
                .best50Duration(minDuration(activities, Activity::getBest50Duration))
                .best100Duration(minDuration(activities, Activity::getBest100Duration))
                .build();
    }

    static String minDuration(List<Activity> activities, Function<Activity, String> getter) {
        String best = null;
        Long bestMs = null;
        for (Activity activity : activities) {
            String raw = getter.apply(activity);
            if (raw == null || raw.isBlank()) {
                continue;
            }
            Long ms = LapTime.toMillis(raw).orElse(null);
            if (ms == null) {
                continue;
            }
            if (bestMs == null || ms < bestMs) {
                bestMs = ms;
                best = raw;
            }
        }
        return best;
    }
}
