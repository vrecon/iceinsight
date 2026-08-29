package nl.templify.iceinsights.services;

import nl.templify.iceinsights.dto.SeasonSummaryDto;
import nl.templify.iceinsights.dto.SeasonTopEntryDto;

import java.util.List;

public interface SeasonQueryService {

    List<SeasonSummaryDto> listCurrentUserSeasons(Long locationId);

    SeasonSummaryDto getCurrentUserSeason(Long id, Long locationId);

    List<SeasonTopEntryDto> listCurrentUserSeasonTop(Long id, Integer n, Integer limit, Long locationId);
}
