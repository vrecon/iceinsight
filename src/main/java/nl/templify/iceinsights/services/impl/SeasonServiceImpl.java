package nl.templify.iceinsights.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import nl.templify.iceinsights.domain.Season;
import nl.templify.iceinsights.domain.SeasonWindow;
import nl.templify.iceinsights.repositories.SeasonRepository;
import nl.templify.iceinsights.services.SeasonService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;

    @Override
    @Transactional
    public Long getOrCreateId(ZonedDateTime instant) {
        LocalDate startDate = SeasonWindow.startDate(instant);
        return seasonRepository.findByStartDate(startDate)
                .map(Season::getId)
                .orElseGet(() -> create(startDate));
    }

    private Long create(LocalDate startDate) {
        Season season = Season.builder()
                .label(SeasonWindow.label(startDate))
                .startDate(startDate)
                .endDate(SeasonWindow.endDate(startDate))
                .build();
        try {
            return seasonRepository.saveAndFlush(season).getId();
        } catch (DataIntegrityViolationException ex) {
            return seasonRepository.findByStartDate(startDate)
                    .map(Season::getId)
                    .orElseThrow(() -> ex);
        }
    }
}
