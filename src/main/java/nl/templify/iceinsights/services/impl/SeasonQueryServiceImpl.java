package nl.templify.iceinsights.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.domain.Chip;
import nl.templify.iceinsights.domain.Season;
import nl.templify.iceinsights.domain.User;
import nl.templify.iceinsights.dto.SeasonSummaryDto;
import nl.templify.iceinsights.dto.SeasonTopEntryDto;
import nl.templify.iceinsights.exceptions.InvalidBestNException;
import nl.templify.iceinsights.exceptions.SeasonNotFoundException;
import nl.templify.iceinsights.repositories.ActivityRepository;
import nl.templify.iceinsights.repositories.SeasonRepository;
import nl.templify.iceinsights.repositories.UserRepository;
import nl.templify.iceinsights.services.AuthenticationFacade;
import nl.templify.iceinsights.services.SeasonQueryService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SeasonQueryServiceImpl implements SeasonQueryService {

    static final Set<Integer> VINK_NS = Set.of(1, 2, 4, 8, 13, 25, 50, 100);
    static final int DEFAULT_TOP_LIMIT = 15;
    static final int MAX_TOP_LIMIT = 50;

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

    @Override
    @Transactional
    public List<SeasonTopEntryDto> listCurrentUserSeasonTop(Long id, Integer n, Integer limit, Long locationId) {
        if (n == null || !VINK_NS.contains(n)) {
            throw new InvalidBestNException("n must be one of 1, 2, 4, 8, 13, 25, 50, 100");
        }
        int cappedLimit = capLimit(limit);
        List<Long> chipIds = currentUserChipIds();
        seasonRepository.findById(id)
                .orElseThrow(() -> new SeasonNotFoundException("Season not found"));
        List<Activity> activities = activitiesFor(chipIds, id, locationId);
        if (activities.isEmpty()) {
            throw new SeasonNotFoundException("Season not found");
        }
        Function<Activity, String> getter = durationGetter(n);
        return activities.stream()
                .map(activity -> toRanked(activity, n, getter))
                .filter(ranked -> ranked != null)
                .sorted(Comparator.comparingLong(RankedActivity::millis))
                .limit(cappedLimit)
                .map(RankedActivity::dto)
                .toList();
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

    static int capLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_TOP_LIMIT;
        }
        if (limit < 1) {
            return 1;
        }
        return Math.min(limit, MAX_TOP_LIMIT);
    }

    static Function<Activity, String> durationGetter(int n) {
        return switch (n) {
            case 1 -> Activity::getBest1Duration;
            case 2 -> Activity::getBest2Duration;
            case 4 -> Activity::getBest4Duration;
            case 8 -> Activity::getBest8Duration;
            case 13 -> Activity::getBest13Duration;
            case 25 -> Activity::getBest25Duration;
            case 50 -> Activity::getBest50Duration;
            case 100 -> Activity::getBest100Duration;
            default -> throw new InvalidBestNException("n must be one of 1, 2, 4, 8, 13, 25, 50, 100");
        };
    }

    private static RankedActivity toRanked(Activity activity, int n, Function<Activity, String> getter) {
        String raw = getter.apply(activity);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        Long ms = LapTime.toMillis(raw).orElse(null);
        if (ms == null) {
            return null;
        }
        return new RankedActivity(SeasonTopEntryDto.builder()
                .activityId(activity.getId())
                .startTime(activity.getStartTime())
                .locationId(activity.getLocationId())
                .chipId(activity.getChipId())
                .n(n)
                .duration(raw)
                .build(), ms);
    }

    private record RankedActivity(SeasonTopEntryDto dto, long millis) {
    }
}
