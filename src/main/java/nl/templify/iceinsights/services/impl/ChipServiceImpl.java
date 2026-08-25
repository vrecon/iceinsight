package nl.templify.iceinsights.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.templify.iceinsights.domain.Chip;
import nl.templify.iceinsights.domain.ChipStatus;
import nl.templify.iceinsights.domain.User;
import nl.templify.iceinsights.dto.ChipDto;
import nl.templify.iceinsights.exceptions.ChipAlreadyLinkedException;
import nl.templify.iceinsights.exceptions.ChipNotFoundException;
import nl.templify.iceinsights.exceptions.ChipNotLinkedException;
import nl.templify.iceinsights.mapper.ChipMapper;
import nl.templify.iceinsights.repositories.ChipRepository;
import nl.templify.iceinsights.repositories.UserRepository;
import nl.templify.iceinsights.services.AuthenticationFacade;
import nl.templify.iceinsights.services.ChipService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChipServiceImpl implements ChipService {

    private final ChipRepository chipRepository;
    private final UserRepository userRepository;
    private final ChipMapper chipMapper;
    private final AuthenticationFacade authFacade;

    @Override
    @Transactional
    public ChipDto linkChipToCurrentUser(String chipCode) {
        User currentUser = getCurrentUser();
        Chip chip = chipRepository.findByChipCode(chipCode)
                .orElseThrow(() -> new ChipNotFoundException("Chip not found with code: " + chipCode));

        if (currentUser.getChips().contains(chip)) {
            throw new ChipAlreadyLinkedException("Chip is already linked to current user");
        }

        currentUser.getChips().add(chip);
        chip.getUsers().add(currentUser);
        userRepository.save(currentUser);

        log.info("Linked chip {} to user {}", chipCode, currentUser.getUsername());

        return chipMapper.toDto(chip);
    }

    @Override
    @Transactional
    public void unlinkChipFromCurrentUser(String chipCode) {
        User currentUser = getCurrentUser();
        Chip chip = chipRepository.findByChipCode(chipCode)
                .orElseThrow(() -> new ChipNotFoundException("Chip not found with code: " + chipCode));

        if (!currentUser.getChips().contains(chip)) {
            throw new ChipNotLinkedException("Chip is not linked to current user");
        }

        currentUser.getChips().remove(chip);
        chip.getUsers().remove(currentUser);
        userRepository.save(currentUser);

        log.info("Unlinked chip {} from user {}", chipCode, currentUser.getUsername());
    }

    @Override
    public List<ChipDto> getCurrentUserChips() {
        User currentUser = getCurrentUser();
        return chipMapper.toDtos(new ArrayList<>(currentUser.getChips()));
    }

    private User getCurrentUser() {
        String username = authFacade.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public Long getOrCreateChipId(String chipCode, String chipLabel) {
        return chipRepository.findByChipCode(chipCode)
                .map(Chip::getId)
                .orElseGet(() -> {
                    Chip newChip = Chip.builder()
                            .chipCode(chipCode)
                            .chipLabel(chipLabel)
                            .status(ChipStatus.ACTIVE)
                            .createdAt(LocalDateTime.now())
                            .users(new HashSet<>())
                            .build();
                    // Flush so activity rows that only store chip_id as a Long
                    // can pass fk_activity_chip in the same JDBC batch.
                    Chip savedChip = chipRepository.saveAndFlush(newChip);
                    log.debug("Created chip {} with id {}", chipCode, savedChip.getId());
                    return savedChip.getId();
                });
    }
}
