package nl.templify.iceinsights.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.domain.Chip;
import nl.templify.iceinsights.domain.Lap;
import nl.templify.iceinsights.domain.Session;
import nl.templify.iceinsights.domain.User;
import nl.templify.iceinsights.dto.ActivityLapDto;
import nl.templify.iceinsights.dto.ActivitySummaryDto;
import nl.templify.iceinsights.exceptions.ActivityNotFoundException;
import nl.templify.iceinsights.mapper.ActivitySummaryMapper;
import nl.templify.iceinsights.repositories.ActivityRepository;
import nl.templify.iceinsights.repositories.SessionRepository;
import nl.templify.iceinsights.repositories.UserRepository;
import nl.templify.iceinsights.services.ActivityQueryService;
import nl.templify.iceinsights.services.AuthenticationFacade;
import nl.templify.iceinsights.services.SessionAnalyticsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ActivityQueryServiceImpl implements ActivityQueryService {

    private final ActivityRepository activityRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final AuthenticationFacade authFacade;
    private final SessionAnalyticsService sessionAnalyticsService;
    private final ActivitySummaryMapper activitySummaryMapper;

    @Override
    @Transactional
    public List<ActivitySummaryDto> listCurrentUserActivities() {
        User user = getCurrentUser();
        List<Long> chipIds = user.getChips().stream().map(Chip::getId).toList();
        if (chipIds.isEmpty()) {
            return List.of();
        }
        return activityRepository.findByChipIdInOrderByStartTimeDesc(chipIds).stream()
                .map(this::toSummaryBackfilling)
                .toList();
    }

    @Override
    @Transactional
    public ActivitySummaryDto getCurrentUserActivity(Long id) {
        return toSummaryBackfilling(requireCurrentUserActivity(id));
    }

    @Override
    @Transactional
    public List<ActivityLapDto> listCurrentUserActivityLaps(Long id) {
        requireCurrentUserActivity(id);
        return sessionRepository.findByActivityIdWithLaps(id).stream()
                .flatMap(session -> lapsOf(session).stream().map(lap -> toLapDto(lap, session)))
                .sorted(Comparator
                        .comparing(ActivityLapDto::getDatetimeStart, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ActivityLapDto::getLapNr, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private Activity requireCurrentUserActivity(Long id) {
        User user = getCurrentUser();
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException("Activity not found"));
        if (!ownsChip(user, activity.getChipId())) {
            throw new ActivityNotFoundException("Activity not found");
        }
        return activity;
    }

    private ActivitySummaryDto toSummaryBackfilling(Activity activity) {
        if (needsBackfill(activity)) {
            List<Session> sessions = sessionRepository.findByActivityIdWithStats(activity.getId());
            sessionAnalyticsService.applyActivityBests(activity, sessions);
            activityRepository.save(activity);
        }
        return activitySummaryMapper.toDto(activity);
    }

    private static List<Lap> lapsOf(Session session) {
        return session.getLaps() == null ? List.of() : session.getLaps();
    }

    private static ActivityLapDto toLapDto(Lap lap, Session session) {
        return ActivityLapDto.builder()
                .lapNr(lap.getLapNr())
                .sessionNr(session.getSessionNr())
                .datetimeStart(lap.getDatetimeStart())
                .duration(lap.getDuration())
                .rest(lap.getRest())
                .movingAvgDuration(lap.getMovingAvgDuration())
                .speedKph(lap.getSpeedKph())
                .build();
    }

    private static boolean needsBackfill(Activity activity) {
        return activity.getBest1Duration() == null
                && activity.getBest2Duration() == null
                && activity.getBest4Duration() == null
                && activity.getBest8Duration() == null
                && activity.getBest13Duration() == null
                && activity.getBest25Duration() == null
                && activity.getBest50Duration() == null
                && activity.getBest100Duration() == null;
    }

    private static boolean ownsChip(User user, Long chipId) {
        Set<Chip> chips = user.getChips();
        if (chips == null || chipId == null) {
            return false;
        }
        return chips.stream().anyMatch(chip -> chipId.equals(chip.getId()));
    }

    private User getCurrentUser() {
        String username = authFacade.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
