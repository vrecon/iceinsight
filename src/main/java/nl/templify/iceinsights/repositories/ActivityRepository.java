package nl.templify.iceinsights.repositories;

import nl.templify.iceinsights.domain.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
   List<Activity> findByChipId(Long chipId);

   List<Activity> findByChipIdInOrderByStartTimeDesc(Collection<Long> chipIds);
}
