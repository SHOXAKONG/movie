package reservation.movie.frameworks.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import reservation.movie.frameworks.db.entity.AuditoriumJpaEntity;

import java.util.List;

public interface AuditoriumJpaRepository extends JpaRepository<AuditoriumJpaEntity, Long> {

    List<AuditoriumJpaEntity> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);
}
