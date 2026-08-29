package nl.templify.iceinsights.controller;

import nl.templify.iceinsights.dto.SeasonSummaryDto;
import nl.templify.iceinsights.dto.SeasonTopEntryDto;
import nl.templify.iceinsights.exceptions.InvalidBestNException;
import nl.templify.iceinsights.exceptions.SeasonNotFoundException;
import nl.templify.iceinsights.services.SeasonQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeasonsControllerTest {

    @Mock
    private SeasonQueryService seasonQueryService;

    @InjectMocks
    private SeasonsController controller;

    @Test
    void list_returnsBodyFromService() {
        SeasonSummaryDto dto = SeasonSummaryDto.builder()
                .id(1L)
                .label("2025/2026")
                .startDate(LocalDate.of(2025, 5, 1))
                .best1Duration("49.000")
                .build();
        when(seasonQueryService.listCurrentUserSeasons(null)).thenReturn(List.of(dto));

        ResponseEntity<List<SeasonSummaryDto>> response = controller.listCurrentUserSeasons(null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals("2025/2026", response.getBody().get(0).getLabel());
        assertEquals("49.000", response.getBody().get(0).getBest1Duration());
    }

    @Test
    void get_missingOrForeign_propagatesNotFound() {
        when(seasonQueryService.getCurrentUserSeason(99L, null))
                .thenThrow(new SeasonNotFoundException("Season not found"));

        assertThrows(SeasonNotFoundException.class, () -> controller.getCurrentUserSeason(99L, null));
    }

    @Test
    void top_returnsBodyFromService() {
        SeasonTopEntryDto dto = SeasonTopEntryDto.builder()
                .activityId(3L)
                .startTime(ZonedDateTime.of(2026, 1, 12, 9, 0, 0, 0, ZoneOffset.UTC))
                .locationId(2822L)
                .chipId(7L)
                .n(13)
                .duration("10:50.000")
                .build();
        when(seasonQueryService.listCurrentUserSeasonTop(5L, 13, 15, null)).thenReturn(List.of(dto));

        ResponseEntity<List<SeasonTopEntryDto>> response =
                controller.listCurrentUserSeasonTop(5L, 13, 15, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(3L, response.getBody().get(0).getActivityId());
        assertEquals("10:50.000", response.getBody().get(0).getDuration());
        assertEquals(13, response.getBody().get(0).getN());
    }

    @Test
    void top_invalidN_propagatesBadRequest() {
        when(seasonQueryService.listCurrentUserSeasonTop(5L, 3, 15, null))
                .thenThrow(new InvalidBestNException("n must be one of 1, 2, 4, 8, 13, 25, 50, 100"));

        assertThrows(InvalidBestNException.class,
                () -> controller.listCurrentUserSeasonTop(5L, 3, 15, null));
    }
}
