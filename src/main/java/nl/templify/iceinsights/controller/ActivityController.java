package nl.templify.iceinsights.controller;

import lombok.AllArgsConstructor;
import nl.templify.iceinsights.services.ActivitySyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity")
@AllArgsConstructor
public class ActivityController {

    private final ActivitySyncService activitySyncService;

    @GetMapping("/sync")
    public ResponseEntity<Void> syncCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        activitySyncService.syncUserActivities(username);
        return ResponseEntity.accepted().build();
    }
}
