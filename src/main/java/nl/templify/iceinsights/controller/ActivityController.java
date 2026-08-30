package nl.templify.iceinsights.controller;

import lombok.AllArgsConstructor;
import nl.templify.iceinsights.api.SyncApi;
import nl.templify.iceinsights.services.ActivitySyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ActivityController implements SyncApi {

    private final ActivitySyncService activitySyncService;

    @Override
    public ResponseEntity<Void> syncCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        activitySyncService.syncUserActivities(username);
        return ResponseEntity.accepted().build();
    }
}
