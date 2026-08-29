package nl.templify.iceinsights.controller;

import lombok.RequiredArgsConstructor;
import nl.templify.iceinsights.dto.ActivityLapDto;
import nl.templify.iceinsights.dto.ActivitySummaryDto;
import nl.templify.iceinsights.services.ActivityQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
public class ActivitiesController {

    private final ActivityQueryService activityQueryService;

    @GetMapping
    public ResponseEntity<List<ActivitySummaryDto>> listCurrentUserActivities() {
        return ResponseEntity.ok(activityQueryService.listCurrentUserActivities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivitySummaryDto> getCurrentUserActivity(@PathVariable Long id) {
        return ResponseEntity.ok(activityQueryService.getCurrentUserActivity(id));
    }

    @GetMapping("/{id}/laps")
    public ResponseEntity<List<ActivityLapDto>> listCurrentUserActivityLaps(@PathVariable Long id) {
        return ResponseEntity.ok(activityQueryService.listCurrentUserActivityLaps(id));
    }
}
