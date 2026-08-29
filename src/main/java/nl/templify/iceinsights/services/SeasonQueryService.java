package nl.templify.iceinsights.services;

import nl.templify.iceinsights.dto.SeasonSummaryDto;

import java.util.List;

public interface SeasonQueryService {

    List<SeasonSummaryDto> listCurrentUserSeasons(Long locationId);

    SeasonSummaryDto getCurrentUserSeason(Long id, Long locationId);
}
