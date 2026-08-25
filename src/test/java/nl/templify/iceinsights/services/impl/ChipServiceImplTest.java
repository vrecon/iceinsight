package nl.templify.iceinsights.services.impl;

import nl.templify.iceinsights.domain.Chip;
import nl.templify.iceinsights.domain.ChipStatus;
import nl.templify.iceinsights.mapper.ChipMapper;
import nl.templify.iceinsights.repositories.ChipRepository;
import nl.templify.iceinsights.repositories.UserRepository;
import nl.templify.iceinsights.services.AuthenticationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChipServiceImplTest {

    @Mock private ChipRepository chipRepository;
    @Mock private UserRepository userRepository;
    @Mock private ChipMapper chipMapper;
    @Mock private AuthenticationFacade authFacade;

    private ChipServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ChipServiceImpl(chipRepository, userRepository, chipMapper, authFacade);
    }

    @Test
    void getOrCreateChipId_returnsExistingIdWithoutInsert() {
        Chip existing = Chip.builder().id(12L).chipCode("PW-39968").build();
        when(chipRepository.findByChipCode("PW-39968")).thenReturn(Optional.of(existing));

        Long id = service.getOrCreateChipId("PW-39968", "PW-39968");

        assertEquals(12L, id);
        verify(chipRepository, never()).saveAndFlush(any());
        verify(chipRepository, never()).save(any());
    }

    @Test
    void getOrCreateChipId_saveAndFlushesNewChipSoFkCanSeeIt() {
        when(chipRepository.findByChipCode("PW-39968")).thenReturn(Optional.empty());
        when(chipRepository.saveAndFlush(any(Chip.class))).thenAnswer(invocation -> {
            Chip chip = invocation.getArgument(0);
            chip.setId(2681L);
            return chip;
        });

        Long id = service.getOrCreateChipId("PW-39968", "label");

        assertEquals(2681L, id);
        ArgumentCaptor<Chip> captor = ArgumentCaptor.forClass(Chip.class);
        verify(chipRepository).saveAndFlush(captor.capture());
        verify(chipRepository, never()).save(any());
        Chip saved = captor.getValue();
        assertEquals("PW-39968", saved.getChipCode());
        assertEquals("label", saved.getChipLabel());
        assertEquals(ChipStatus.ACTIVE, saved.getStatus());
    }
}
