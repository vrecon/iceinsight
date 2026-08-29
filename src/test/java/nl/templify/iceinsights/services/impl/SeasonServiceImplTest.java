package nl.templify.iceinsights.services.impl;

import nl.templify.iceinsights.domain.Season;
import nl.templify.iceinsights.repositories.SeasonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeasonServiceImplTest {

    @Mock
    private SeasonRepository seasonRepository;

    private SeasonServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SeasonServiceImpl(seasonRepository);
    }

    @Test
    void getOrCreateId_existingStartDate_isIdempotent() {
        LocalDate start = LocalDate.of(2025, 5, 1);
        Season existing = Season.builder()
                .id(12L)
                .label("2025/2026")
                .startDate(start)
                .endDate(LocalDate.of(2026, 4, 30))
                .build();
        when(seasonRepository.findByStartDate(start)).thenReturn(Optional.of(existing));

        Long id = service.getOrCreateId(ZonedDateTime.parse("2026-01-15T10:00:00+01:00[Europe/Amsterdam]"));

        assertEquals(12L, id);
        verify(seasonRepository, never()).saveAndFlush(any());
    }

    @Test
    void getOrCreateId_missing_createsMayToAprilRow() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        when(seasonRepository.findByStartDate(start)).thenReturn(Optional.empty());
        when(seasonRepository.saveAndFlush(any(Season.class))).thenAnswer(invocation -> {
            Season season = invocation.getArgument(0);
            season.setId(3L);
            return season;
        });

        Long id = service.getOrCreateId(ZonedDateTime.parse("2026-08-29T12:00:00+02:00[Europe/Amsterdam]"));

        assertEquals(3L, id);
        ArgumentCaptor<Season> captor = ArgumentCaptor.forClass(Season.class);
        verify(seasonRepository).saveAndFlush(captor.capture());
        Season created = captor.getValue();
        assertEquals("2026/2027", created.getLabel());
        assertEquals(LocalDate.of(2026, 5, 1), created.getStartDate());
        assertEquals(LocalDate.of(2027, 4, 30), created.getEndDate());
    }
}
