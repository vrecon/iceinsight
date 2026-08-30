package nl.templify.iceinsights.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.templify.iceinsights.api.ChipsApi;
import nl.templify.iceinsights.dto.ChipDto;
import nl.templify.iceinsights.services.ChipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChipController implements ChipsApi {

    private final ChipService chipService;

    @Override
    public ResponseEntity<ChipDto> linkChipToCurrentUser(String chipCode) {
        return ResponseEntity.ok(chipService.linkChipToCurrentUser(chipCode));
    }

    @Override
    public ResponseEntity<Void> unlinkChipFromCurrentUser(String chipCode) {
        chipService.unlinkChipFromCurrentUser(chipCode);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<ChipDto>> getCurrentUserChips() {
        return ResponseEntity.ok(chipService.getCurrentUserChips());
    }
}
