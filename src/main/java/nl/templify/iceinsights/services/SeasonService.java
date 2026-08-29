package nl.templify.iceinsights.services;

import java.time.ZonedDateTime;

public interface SeasonService {

    Long getOrCreateId(ZonedDateTime instant);
}
