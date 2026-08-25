package nl.templify.iceinsights.services;

import jakarta.transaction.Transactional;
import nl.templify.iceinsights.dto.ActivityDto;

import java.util.List;

public interface ActivityImportService {
    void importActivities(Long locationId);
    List<ActivityDto> fetchActivitiesBatch(Long locationId, int offset);
}
