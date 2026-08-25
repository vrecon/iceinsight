package nl.templify.iceinsights.services.impl;

import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.dto.ActivityDto;
import nl.templify.iceinsights.dto.ActivityResponseDto;
import nl.templify.iceinsights.repositories.ActivityRepository;
import nl.templify.iceinsights.services.ChipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityImportServiceImplTest {

    @Mock
    private WebClient webClient;
    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private ChipService chipService;
    @Mock
    private PlatformTransactionManager transactionManager;

    private ActivityImportServiceImpl service;

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        service = spy(new ActivityImportServiceImpl(webClient, activityRepository, chipService, transactionManager));
    }

    @Test
    void importActivities_savesMappedEntitiesAndStopsAtMax() {
        ActivityDto dto = new ActivityDto();
        dto.setId(11L);
        dto.setName("Practice");
        dto.setStartTime(ZonedDateTime.parse("2026-01-15T10:00:00Z"));
        dto.setEndTime(ZonedDateTime.parse("2026-01-15T11:00:00Z"));
        dto.setChipCode("CHIP-1");
        dto.setChipLabel("Jan");

        ActivityResponseDto response = new ActivityResponseDto();
        response.setActivityCount(355228);
        response.setActivities(List.of(dto));

        doReturn(response).when(service).fetchActivitiesResponse(eq(2822L), eq(0), eq(2026), eq(1));
        when(chipService.getOrCreateChipId("CHIP-1", "Jan")).thenReturn(99L);

        int imported = service.importActivities(2822L, 2026, 1);

        assertEquals(1, imported);
        verify(service).fetchActivitiesResponse(2822L, 0, 2026, 1);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Activity>> captor = ArgumentCaptor.forClass(List.class);
        verify(activityRepository).saveAll(captor.capture());
        Activity saved = captor.getValue().get(0);
        assertEquals(11L, saved.getId());
        assertEquals("Practice", saved.getName());
        assertEquals(2822L, saved.getLocationId());
        assertEquals(99L, saved.getChipId());
        assertEquals(dto.getStartTime(), saved.getStartTime());
        assertEquals(dto.getEndTime(), saved.getEndTime());
    }

    @Test
    void importActivities_nullActivities_doesNotThrowAndSavesNothing() {
        ActivityResponseDto response = new ActivityResponseDto();
        response.setActivityCount(355228);
        response.setActivities(null);

        doReturn(response).when(service).fetchActivitiesResponse(eq(2822L), eq(0), eq(2026), eq(10));

        int imported = service.importActivities(2822L, 2026, 10);

        assertEquals(0, imported);
        verify(activityRepository, never()).saveAll(any());
    }

    @Test
    void importActivities_emptyListStopsWithoutSave() {
        ActivityResponseDto response = new ActivityResponseDto();
        response.setActivityCount(0);
        response.setActivities(Collections.emptyList());

        doReturn(response).when(service).fetchActivitiesResponse(anyLong(), anyInt(), any(), anyInt());

        int imported = service.importActivities(2822L, 2026, 500);

        assertEquals(0, imported);
        verify(activityRepository, never()).saveAll(any());
    }

    @Test
    void importActivities_nullMaxDefaultsTo500AndRequestsMinBatch() {
        ActivityDto dto = new ActivityDto();
        dto.setId(1L);
        dto.setName("Practice");
        dto.setChipCode("C");
        dto.setChipLabel("L");

        ActivityResponseDto response = new ActivityResponseDto();
        response.setActivityCount(12);
        response.setActivities(List.of(dto));

        doReturn(response).when(service).fetchActivitiesResponse(eq(2822L), eq(0), eq(2026), eq(200));
        when(chipService.getOrCreateChipId("C", "L")).thenReturn(1L);

        int imported = service.importActivities(2822L, 2026, null);

        assertEquals(1, imported);
        verify(service).fetchActivitiesResponse(2822L, 0, 2026, 200);
        assertTrue(imported < 500);
    }
}
