package nl.templify.iceinsights.controller;

import lombok.RequiredArgsConstructor;
import nl.templify.iceinsights.api.ActivitiesApi;
import nl.templify.iceinsights.dto.ActivityLapDto;
import nl.templify.iceinsights.dto.ActivitySummaryDto;
import nl.templify.iceinsights.services.ActivityQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ActivitiesController implements ActivitiesApi {

    private final ActivityQueryService activityQueryService;

    @Override
    public ResponseEntity<List<ActivitySummaryDto>> listCurrentUserActivities() {
        return ResponseEntity.ok(activityQueryService.listCurrentUserActivities());
    }

    @Override
    public ResponseEntity<ActivitySummaryDto> getCurrentUserActivity(Long id) {
        return ResponseEntity.ok(activityQueryService.getCurrentUserActivity(id));
    }

    @Override
    public ResponseEntity<List<ActivityLapDto>> listCurrentUserActivityLaps(Long id) {
        return ResponseEntity.ok(activityQueryService.listCurrentUserActivityLaps(id));
    }
}
