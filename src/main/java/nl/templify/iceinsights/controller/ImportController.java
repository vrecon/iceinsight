package nl.templify.iceinsights.controller;

import lombok.AllArgsConstructor;
import nl.templify.iceinsights.api.ImportApi;
import nl.templify.iceinsights.services.ActivityImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;

@RestController
@AllArgsConstructor
public class ImportController implements ImportApi {

    static final int DEFAULT_MAX = 500;

    private final ActivityImportService importService;

    /**
     * Import practice activities from Speedhive for a location.
     * Defaults to the current year and at most 500 rows so large locations
     * (e.g. Haarlem 2822, ~355k activities) stay practical.
     * Pass {@code year} and {@code max} as query parameters to override.
     */
    @Override
    public ResponseEntity<String> importActivities(Long locationId, Integer year, Integer max) {
        int importYear = year != null ? year : Year.now().getValue();
        int importMax = (max == null || max < 1) ? DEFAULT_MAX : max;
        int imported = importService.importActivities(locationId, importYear, importMax);
        return ResponseEntity.ok(
                "Imported " + imported + " activities for location " + locationId
                        + " (year " + importYear + ", max " + importMax + ")");
    }
}
