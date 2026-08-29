package nl.templify.iceinsights.repositories;

import nl.templify.iceinsights.domain.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
   List<Activity> findByChipId(Long chipId);

   List<Activity> findByChipIdInOrderByStartTimeDesc(Collection<Long> chipIds);

   List<Activity> findByChipIdInAndSeasonId(Collection<Long> chipIds, Long seasonId);

   List<Activity> findByChipIdInAndSeasonIdAndLocationId(Collection<Long> chipIds, Long seasonId, Long locationId);

   @Query("SELECT DISTINCT a.seasonId FROM Activity a WHERE a.chipId IN :chipIds AND a.seasonId IS NOT NULL")
   List<Long> findDistinctSeasonIdsByChipIdIn(@Param("chipIds") Collection<Long> chipIds);

   @Query("SELECT DISTINCT a.seasonId FROM Activity a WHERE a.chipId IN :chipIds AND a.locationId = :locationId AND a.seasonId IS NOT NULL")
   List<Long> findDistinctSeasonIdsByChipIdInAndLocationId(@Param("chipIds") Collection<Long> chipIds,
                                                           @Param("locationId") Long locationId);
}
