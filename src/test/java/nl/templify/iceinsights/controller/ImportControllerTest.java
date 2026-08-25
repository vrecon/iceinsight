package nl.templify.iceinsights.controller;

import nl.templify.iceinsights.services.ActivityImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportControllerTest {

    @Mock
    private ActivityImportService importService;

    @InjectMocks
    private ImportController importController;

    @Test
    void importActivities_defaultsToCurrentYearAndMax500() {
        int currentYear = Year.now().getValue();
        when(importService.importActivities(2822L, currentYear, 500)).thenReturn(42);

        ResponseEntity<String> response = importController.importActivities(2822L, null, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Imported 42 activities for location 2822 (year " + currentYear + ", max 500)",
                response.getBody());
        verify(importService).importActivities(2822L, currentYear, 500);
    }

    @Test
    void importActivities_usesExplicitYearAndMax() {
        when(importService.importActivities(2822L, 2024, 10)).thenReturn(10);

        ResponseEntity<String> response = importController.importActivities(2822L, 2024, 10);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Imported 10 activities for location 2822 (year 2024, max 10)", response.getBody());
        verify(importService).importActivities(2822L, 2024, 10);
    }
}
