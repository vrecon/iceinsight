package nl.templify.iceinsights.services;

public interface ActivityImportService {

    /**
     * Import practice activities for a Speedhive location.
     *
     * @param locationId Speedhive location id
     * @param year       year filter; {@code null} means no year filter
     * @param max        maximum rows to save; {@code null} or &lt; 1 is treated as 500
     * @return number of activities saved
     */
    int importActivities(Long locationId, Integer year, Integer max);
}
