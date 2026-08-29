package nl.templify.iceinsights.controller;

import nl.templify.iceinsights.dto.ActivitySummaryDto;
import nl.templify.iceinsights.exceptions.ActivityNotFoundException;
import nl.templify.iceinsights.services.ActivityQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivitiesControllerTest {

    @Mock
    private ActivityQueryService activityQueryService;

    @InjectMocks
    private ActivitiesController controller;

    @Test
    void list_returnsBodyFromService() {
        ActivitySummaryDto dto = ActivitySummaryDto.builder().id(1L).best1Duration("49.000").build();
        when(activityQueryService.listCurrentUserActivities()).thenReturn(List.of(dto));

        ResponseEntity<List<ActivitySummaryDto>> response = controller.listCurrentUserActivities();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals("49.000", response.getBody().get(0).getBest1Duration());
    }

    @Test
    void get_missingOrForeign_propagatesNotFound() {
        when(activityQueryService.getCurrentUserActivity(99L))
                .thenThrow(new ActivityNotFoundException("Activity not found"));

        assertThrows(ActivityNotFoundException.class, () -> controller.getCurrentUserActivity(99L));
    }
}
