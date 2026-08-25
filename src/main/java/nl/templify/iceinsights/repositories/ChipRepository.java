package nl.templify.iceinsights.repositories;

import nl.templify.iceinsights.domain.Chip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChipRepository extends JpaRepository<Chip, Long> {
    Optional<Chip> findByChipCode(String chipCode);
}