package nl.templify.iceinsights.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.dto.ActivityDto;
import nl.templify.iceinsights.dto.ActivityResponseDto;
import nl.templify.iceinsights.mapper.ActivityMapper;
import nl.templify.iceinsights.repositories.ActivityRepository;
import nl.templify.iceinsights.services.ActivityImportService;
import nl.templify.iceinsights.services.ChipService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityImportServiceImpl implements ActivityImportService {

    private static final String API_PATH = "/api/v1/locations/%d/activities";
    private static final int BATCH_SIZE = 200;

    private final WebClient webClient;
    private final ActivityRepository activityRepository;
    private final ChipService chipService;
    private final ActivityMapper activityMapper;

    @Override
    @Transactional
    public void importActivities(Long locationId) {
        int offset = 0;
        int totalImported = 0;
        boolean hasMoreData = true;

        while (hasMoreData && offset < 30000) {
            try {
                List<ActivityDto> activities = fetchActivitiesBatch(locationId, offset);

                if (activities.isEmpty()) {
                    hasMoreData = false;
                    log.info("No more activities to import. Total imported: {}", totalImported);
                    break;
                }

                saveActivities(activities,locationId);

                totalImported += activities.size();
                offset += BATCH_SIZE;

                log.info("Imported batch of {} activities. Total imported so far: {}",
                        activities.size(), totalImported);

                Thread.sleep(500);

            } catch (Exception e) {
                log.error("Error importing activities at offset {}: {}", offset, e.getMessage());
                throw new RuntimeException("Failed to import activities", e);
            }
        }
    }

    @Override
    public List<ActivityDto> fetchActivitiesBatch(Long locationId, int offset) {
        String path = String.format(API_PATH, locationId);

        log.info("Fetching activities from path: {}", path);

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("count", BATCH_SIZE)
                        .queryParam("offset", offset)
                        .build())
                .retrieve()
                .bodyToMono(ActivityResponseDto.class)
                .map(response -> {
                    log.info("Received {} activities", response.getActivities().size());
                    return response.getActivities();
                })
                .block();
    }

    @Transactional
    protected void saveActivities(List<ActivityDto> activities, Long locationId) {
        activities.stream()
                .map(dto -> {
                    Long chipId = chipService.getOrCreateChipId(dto.getChipCode(), dto.getChipLabel());
                    return Activity.builder()
                            .id(dto.getId())
                            .name(dto.getName())
                            .startTime(dto.getStartTime())
                            .endTime(dto.getEndTime())
                            .locationId(locationId)
                            .chipId(chipId)
                            .build();
                })
                .forEach(activityRepository::save);
    }

}