package nl.templify.iceinsights.repositories;

import nl.templify.iceinsights.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    boolean existsByActivityId(Long activityId);
    List<Session> findByActivityId(Long activityId);

    @Query("select s from Session s left join fetch s.stats where s.activity.id = :activityId")
    List<Session> findByActivityIdWithStats(@Param("activityId") Long activityId);
}
