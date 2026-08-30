package nl.templify.iceinsights.controller;

import lombok.RequiredArgsConstructor;
import nl.templify.iceinsights.api.SeasonsApi;
import nl.templify.iceinsights.dto.SeasonSummaryDto;
import nl.templify.iceinsights.dto.SeasonTopEntryDto;
import nl.templify.iceinsights.services.SeasonQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SeasonsController implements SeasonsApi {

    private final SeasonQueryService seasonQueryService;

    @Override
    public ResponseEntity<List<SeasonSummaryDto>> listCurrentUserSeasons(Long locationId) {
        return ResponseEntity.ok(seasonQueryService.listCurrentUserSeasons(locationId));
    }

    @Override
    public ResponseEntity<SeasonSummaryDto> getCurrentUserSeason(Long id, Long locationId) {
        return ResponseEntity.ok(seasonQueryService.getCurrentUserSeason(id, locationId));
    }

    @Override
    public ResponseEntity<List<SeasonTopEntryDto>> listCurrentUserSeasonTop(
            Long id, Integer n, Integer limit, Long locationId) {
        return ResponseEntity.ok(seasonQueryService.listCurrentUserSeasonTop(id, n, limit, locationId));
    }
}
