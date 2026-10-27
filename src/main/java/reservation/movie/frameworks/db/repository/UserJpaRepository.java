package reservation.movie.frameworks.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import reservation.movie.frameworks.db.entity.UserJpaEntity;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
