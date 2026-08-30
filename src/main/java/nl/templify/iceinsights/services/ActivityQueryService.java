package nl.templify.iceinsights.services;

import nl.templify.iceinsights.dto.ActivityLapDto;
import nl.templify.iceinsights.dto.ActivitySummaryDto;

import java.util.List;

public interface ActivityQueryService {

    List<ActivitySummaryDto> listCurrentUserActivities();

    ActivitySummaryDto getCurrentUserActivity(Long id);

    List<ActivityLapDto> listCurrentUserActivityLaps(Long id);
}
