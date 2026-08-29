package nl.templify.iceinsights.repositories;

import nl.templify.iceinsights.domain.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {
    Optional<Season> findByStartDate(LocalDate startDate);

    List<Season> findByIdInOrderByStartDateDesc(Collection<Long> ids);
}
