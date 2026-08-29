package nl.templify.iceinsights.mapper;

import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.dto.ActivitySummaryDto;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ActivitySummaryMapperTest {

    private final ActivitySummaryMapper mapper = new ActivitySummaryMapper();

    @Test
    void toDto_copiesIdentityAndBests() {
        Activity activity = Activity.builder()
                .id(8190779733L)
                .name("Practice")
                .startTime(ZonedDateTime.parse("2026-08-20T09:33:43+02:00"))
                .endTime(ZonedDateTime.parse("2026-08-20T11:00:00+02:00"))
                .locationId(2822L)
                .chipId(7L)
                .best1Duration("49.000")
                .best2Duration("1:40.000")
                .best5Duration("4:10.000")
                .best13Duration(null)
                .best25Duration(null)
                .build();

        ActivitySummaryDto dto = mapper.toDto(activity);

        assertEquals(8190779733L, dto.getId());
        assertEquals("Practice", dto.getName());
        assertEquals(activity.getStartTime(), dto.getStartTime());
        assertEquals(activity.getEndTime(), dto.getEndTime());
        assertEquals(2822L, dto.getLocationId());
        assertEquals(7L, dto.getChipId());
        assertEquals("49.000", dto.getBest1Duration());
        assertEquals("1:40.000", dto.getBest2Duration());
        assertEquals("4:10.000", dto.getBest5Duration());
        assertNull(dto.getBest13Duration());
        assertNull(dto.getBest25Duration());
    }

    @Test
    void toDto_nullActivity_returnsNull() {
        assertNull(mapper.toDto(null));
    }
}
