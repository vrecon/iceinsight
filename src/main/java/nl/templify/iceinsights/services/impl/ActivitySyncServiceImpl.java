package nl.templify.iceinsights.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.domain.Session;
import nl.templify.iceinsights.domain.User;
import nl.templify.iceinsights.dto.ActivityDetailsResponse;
import nl.templify.iceinsights.mapper.ActivityDetailsMapper;
import nl.templify.iceinsights.repositories.ActivityRepository;
import nl.templify.iceinsights.repositories.SessionRepository;
import nl.templify.iceinsights.repositories.UserRepository;
import nl.templify.iceinsights.services.ActivitySyncService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivitySyncServiceImpl implements ActivitySyncService {

    private static final String SESSIONS_PATH = "/api/v1/training/activities/{id}/sessions";

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final SessionRepository sessionRepository;
    private final WebClient webClient;
    private final ActivityDetailsMapper detailsMapper;

    @Override
    @Transactional
    public void syncUserActivities(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        log.info("Starting sync for user: {} with {} chips", username, user.getChips().size());

        user.getChips().forEach(chip -> {
            List<Activity> activities = activityRepository.findByChipId(chip.getId());
            activities.forEach(activity -> {
                if (!sessionRepository.existsByActivityId(activity.getId())) {
                    fetchAndSaveActivityDetails(activity);
                }
            });
        });
    }

    private void fetchAndSaveActivityDetails(Activity activity) {
        log.info("Fetching details for activity: {}", activity.getId());

        try {
            ActivityDetailsResponse details = webClient
                    .get()
                    .uri(SESSIONS_PATH, activity.getId())
                    .retrieve()
                    .bodyToMono(ActivityDetailsResponse.class)
                    .block();

            if (details != null) {
                List<Session> sessions = detailsMapper.mapToEntities(details, activity);
                sessionRepository.saveAll(sessions);
                log.info("Successfully saved {} sessions for activity: {}",
                        sessions.size(), activity.getId());
            }
        } catch (Exception e) {
            log.error("Error fetching details for activity {}: {}", activity.getId(), e.getMessage());
        }
    }
}
