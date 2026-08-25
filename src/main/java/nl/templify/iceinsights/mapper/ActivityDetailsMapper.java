package nl.templify.iceinsights.mapper;

import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.domain.Session;
import nl.templify.iceinsights.dto.ActivityDetailsResponse;

import java.util.List;

public interface ActivityDetailsMapper {

    List<Session> mapToEntities(ActivityDetailsResponse response, Activity activity);
}
