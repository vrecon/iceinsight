package nl.templify.iceinsights.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.domain.Chip;
import nl.templify.iceinsights.domain.Session;
import nl.templify.iceinsights.domain.User;
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
        User user = getCurrentUser();
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException("Activity not found"));
        if (!ownsChip(user, activity.getChipId())) {
            throw new ActivityNotFoundException("Activity not found");
        }
        return toSummaryBackfilling(activity);
    }

    private ActivitySummaryDto toSummaryBackfilling(Activity activity) {
        if (needsBackfill(activity)) {
            List<Session> sessions = sessionRepository.findByActivityIdWithStats(activity.getId());
            sessionAnalyticsService.applyActivityBests(activity, sessions);
            activityRepository.save(activity);
        }
        return activitySummaryMapper.toDto(activity);
    }

    private static boolean needsBackfill(Activity activity) {
        return activity.getBest1Duration() == null
                && activity.getBest2Duration() == null
                && activity.getBest5Duration() == null
                && activity.getBest13Duration() == null
                && activity.getBest25Duration() == null;
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
