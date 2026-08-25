package nl.templify.iceinsights.controller;

import lombok.AllArgsConstructor;
import nl.templify.iceinsights.services.ActivityImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/import")
@AllArgsConstructor
public class ImportController {
    
    private final ActivityImportService importService;
    
    @PostMapping("/activities/{locationId}")
    public ResponseEntity<String> importActivities(@PathVariable Long locationId) {
        importService.importActivities(locationId);
        return ResponseEntity.ok("Import started");
    }
}