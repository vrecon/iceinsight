package nl.templify.iceinsights.services.impl;

import lombok.extern.slf4j.Slf4j;
import nl.templify.iceinsights.domain.Activity;
import nl.templify.iceinsights.dto.ActivityDto;
import nl.templify.iceinsights.dto.ActivityResponseDto;
import nl.templify.iceinsights.repositories.ActivityRepository;
import nl.templify.iceinsights.services.ActivityImportService;
import nl.templify.iceinsights.services.ChipService;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ActivityImportServiceImpl implements ActivityImportService {

    private static final String API_PATH = "/api/v1/locations/%d/activities";
    private static final int BATCH_SIZE = 200;
    private static final int DEFAULT_MAX = 500;
    private static final long BATCH_PAUSE_MS = 500L;

    private final WebClient webClient;
    private final ActivityRepository activityRepository;
    private final ChipService chipService;
    private final TransactionTemplate transactionTemplate;

    public ActivityImportServiceImpl(WebClient webClient,
                                     ActivityRepository activityRepository,
                                     ChipService chipService,
                                     PlatformTransactionManager transactionManager) {
        this.webClient = webClient;
        this.activityRepository = activityRepository;
        this.chipService = chipService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public int importActivities(Long locationId, Integer year, Integer max) {
        int maxToImport = (max == null || max < 1) ? DEFAULT_MAX : max;
        int offset = 0;
        int totalImported = 0;
        Integer activityCount = null;

        while (totalImported < maxToImport) {
            int remaining = maxToImport - totalImported;
            int count = Math.min(BATCH_SIZE, remaining);
            ActivityResponseDto response = fetchActivitiesResponse(locationId, offset, year, count);
            if (activityCount == null && response.getActivityCount() != null) {
                activityCount = response.getActivityCount();
                log.info("Speedhive reported activityCount={} for location {} (year={})",
                        activityCount, locationId, year);
            }

            List<ActivityDto> activities = response.getActivities() != null
                    ? response.getActivities()
                    : Collections.emptyList();

            if (activities.size() > remaining) {
                activities = List.copyOf(activities.subList(0, remaining));
            }

            if (activities.isEmpty()) {
                log.info("No more activities to import. Saved {} of {} for location {} (year={}, max={})",
                        totalImported, activityCount, locationId, year, maxToImport);
                break;
            }

            saveActivities(activities, locationId);

            totalImported += activities.size();
            offset += activities.size();

            log.info("Imported batch of {} activities. Saved {} of {} for location {} (year={}, max={})",
                    activities.size(), totalImported, activityCount, locationId, year, maxToImport);

            if (activities.size() < count) {
                log.info("Last page received ({} < {}). Stopping.", activities.size(), count);
                break;
            }

            if (totalImported < maxToImport) {
                try {
                    Thread.sleep(BATCH_PAUSE_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Activity import interrupted", e);
                }
            }
        }

        log.info("Import finished. Saved {} of {} activities for location {} (year={}, max={})",
                totalImported, activityCount, locationId, year, maxToImport);
        return totalImported;
    }

    ActivityResponseDto fetchActivitiesResponse(Long locationId, int offset, Integer year, int count) {
        String path = String.format(API_PATH, locationId);
        log.info("Fetching activities from path={} count={} offset={} year={}", path, count, offset, year);

        ActivityResponseDto response = webClient
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path(path)
                            .queryParam("count", count)
                            .queryParam("offset", offset);
                    if (year != null) {
                        uriBuilder.queryParam("year", year);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> {
                                    log.error("Speedhive HTTP {} for location {} offset {}: {}",
                                            clientResponse.statusCode(), locationId, offset, body);
                                    return Mono.error(new RuntimeException(
                                            "Speedhive API request failed: HTTP "
                                                    + clientResponse.statusCode() + " " + body));
                                }))
                .bodyToMono(ActivityResponseDto.class)
                .block();

        if (response == null) {
            ActivityResponseDto empty = new ActivityResponseDto();
            empty.setActivities(Collections.emptyList());
            return empty;
        }
        return response;
    }

    private void saveActivities(List<ActivityDto> activities, Long locationId) {
        transactionTemplate.executeWithoutResult(status -> {
            List<Activity> entities = activities.stream()
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
                    .toList();
            activityRepository.saveAll(entities);
        });
    }
}
