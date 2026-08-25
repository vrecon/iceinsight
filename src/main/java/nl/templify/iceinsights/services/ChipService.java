package nl.templify.iceinsights.services;

import nl.templify.iceinsights.dto.AddChipRequest;
import nl.templify.iceinsights.dto.ChipDto;
import nl.templify.iceinsights.exceptions.NotAuthenticatedException;

import java.util.List;

// ChipService.java
public interface ChipService {
    ChipDto linkChipToCurrentUser(String chipCode);
    void unlinkChipFromCurrentUser(String chipCode);
    List<ChipDto> getCurrentUserChips();

    Long getOrCreateChipId(String chipCode, String chipLabel);
}

