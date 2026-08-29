package nl.templify.iceinsights.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class LapDtoJacksonTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void dataAttributes_objectsFromSpeedhiveDoNotFail() throws Exception {
        String json = """
                {
                  "nr": 1,
                  "dateTimeStart": "2024-12-14T13:52:00+01:00",
                  "duration": "1:16.000",
                  "speed": {"kph": 18.9, "mps": 5.25},
                  "sessionDuration": "1:16.000",
                  "status": "NONE",
                  "sections": [],
                  "dataAttributes": [
                    {"type": "VOLTAGE", "value": 1.2},
                    {"type": "TEMPERATURE", "value": 42.0}
                  ]
                }
                """;

        LapDto lap = mapper.readValue(json, LapDto.class);
        assertEquals(1, lap.getNr());
        assertEquals(2, lap.getDataAttributes().size());
        assertEquals("VOLTAGE", lap.getDataAttributes().get(0).getType());
        assertInstanceOf(Number.class, lap.getDataAttributes().get(0).getValue());
        assertEquals("TEMPERATURE", lap.getDataAttributes().get(1).getType());
    }

    @Test
    void activityDetails_sessionsPayloadWithDataAttributesDecodes() throws Exception {
        String json = """
                {
                  "bestLap": {"sessionId": 1, "lapNr": 7, "duration": "1:03.173", "speed": {"kph": 22.7, "mps": 6.3}},
                  "stats": {
                    "lapCount": 106,
                    "fastestTime": "1:03.173",
                    "averageTime": "1:16.084",
                    "chip": {"code": "NR-52139", "codeNr": 110515435, "carId": 0, "id": 1105154350}
                  },
                  "sessions": [{
                    "id": 1,
                    "chipId": 1105154350,
                    "dateTimeStart": "2024-12-14T13:52:00+01:00",
                    "bestLap": {"nr": 7, "duration": "1:03.173", "speed": {"kph": 22.7, "mps": 6.3}},
                    "aveLapDuration": "1:16.084",
                    "duration": "12:40.000",
                    "laps": [{
                      "nr": 1,
                      "dateTimeStart": "2024-12-14T13:52:00+01:00",
                      "duration": "1:16.000",
                      "speed": {"kph": 18.9, "mps": 5.25},
                      "status": "NONE",
                      "sections": [],
                      "dataAttributes": [{"type": "VOLTAGE", "value": 1.2}]
                    }]
                  }],
                  "sections": [{"name": "Lap", "length": 400, "speedTrap": false}]
                }
                """;

        ActivityDetailsResponse details = mapper.readValue(json, ActivityDetailsResponse.class);
        assertEquals(1, details.getSessions().size());
        assertEquals("VOLTAGE", details.getSessions().get(0).getLaps().get(0).getDataAttributes().get(0).getType());
    }
}
