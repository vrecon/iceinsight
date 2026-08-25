package nl.templify.iceinsights.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.templify.iceinsights.dto.AddChipRequest;
import nl.templify.iceinsights.dto.ChipDto;
import nl.templify.iceinsights.exceptions.NotAuthenticatedException;
import nl.templify.iceinsights.services.ChipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/users/chips")
@Slf4j
@RequiredArgsConstructor
public class ChipController {

    private final ChipService chipService;

    @PostMapping("/{chipCode}")
    public ResponseEntity<ChipDto> linkChipToCurrentUser(@PathVariable String chipCode) {
        return ResponseEntity.ok(chipService.linkChipToCurrentUser(chipCode));
    }

    @DeleteMapping("/{chipCode}")
    public ResponseEntity<Void> unlinkChipFromCurrentUser(@PathVariable String chipCode) {
        chipService.unlinkChipFromCurrentUser(chipCode);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<ChipDto>> getCurrentUserChips() {
        return ResponseEntity.ok(chipService.getCurrentUserChips());
    }
}