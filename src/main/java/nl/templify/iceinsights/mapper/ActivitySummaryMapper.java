package nl.templify.iceinsights.mapper;

import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.dto.ActivitySummaryDto;
import org.springframework.stereotype.Component;

@Component
public class ActivitySummaryMapper {

    public ActivitySummaryDto toDto(Activity activity) {
        if (activity == null) {
            return null;
        }
        return ActivitySummaryDto.builder()
                .id(activity.getId())
                .name(activity.getName())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .locationId(activity.getLocationId())
                .chipId(activity.getChipId())
                .best1Duration(activity.getBest1Duration())
                .best2Duration(activity.getBest2Duration())
                .best4Duration(activity.getBest4Duration())
                .best8Duration(activity.getBest8Duration())
                .best13Duration(activity.getBest13Duration())
                .best25Duration(activity.getBest25Duration())
                .best50Duration(activity.getBest50Duration())
                .best100Duration(activity.getBest100Duration())
                .build();
    }
}
