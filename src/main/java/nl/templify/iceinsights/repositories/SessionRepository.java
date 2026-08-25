package nl.templify.iceinsights.repositories;

import nl.templify.iceinsights.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    boolean existsByActivityId(Long activityId);
    List<Session> findByActivityId(Long activityId);
}