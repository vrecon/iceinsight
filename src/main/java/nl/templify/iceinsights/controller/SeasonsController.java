package nl.templify.iceinsights.controller;

import lombok.RequiredArgsConstructor;
import nl.templify.iceinsights.dto.SeasonSummaryDto;
import nl.templify.iceinsights.dto.SeasonTopEntryDto;
import nl.templify.iceinsights.services.SeasonQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seasons")
@RequiredArgsConstructor
public class SeasonsController {

    private final SeasonQueryService seasonQueryService;

    @GetMapping
    public ResponseEntity<List<SeasonSummaryDto>> listCurrentUserSeasons(
            @RequestParam(required = false) Long locationId) {
        return ResponseEntity.ok(seasonQueryService.listCurrentUserSeasons(locationId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeasonSummaryDto> getCurrentUserSeason(
            @PathVariable Long id,
            @RequestParam(required = false) Long locationId) {
        return ResponseEntity.ok(seasonQueryService.getCurrentUserSeason(id, locationId));
    }

    @GetMapping("/{id}/top")
    public ResponseEntity<List<SeasonTopEntryDto>> listCurrentUserSeasonTop(
            @PathVariable Long id,
            @RequestParam Integer n,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Long locationId) {
        return ResponseEntity.ok(seasonQueryService.listCurrentUserSeasonTop(id, n, limit, locationId));
    }
}
