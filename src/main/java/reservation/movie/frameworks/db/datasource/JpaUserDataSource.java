package reservation.movie.frameworks.db.datasource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reservation.movie.adapters.gateways.user.UserDataSource;
import reservation.movie.adapters.gateways.user.UserRecord;
import reservation.movie.frameworks.db.entity.UserJpaEntity;
import reservation.movie.frameworks.db.repository.UserJpaRepository;

import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class JpaUserDataSource implements UserDataSource {

    private final UserJpaRepository repository;

    public JpaUserDataSource(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public UserRecord save(UserRecord user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.id());
        entity.setEmail(user.email());
        entity.setPasswordHash(user.passwordHash());
        entity.setFullName(user.fullName());
        entity.setRole(user.role());
        return toRecord(repository.save(entity));
    }

    @Override
    public Optional<UserRecord> findById(Long id) {
        return repository.findById(id).map(JpaUserDataSource::toRecord);
    }

    @Override
    public Optional<UserRecord> findByEmail(String email) {
        return repository.findByEmail(email).map(JpaUserDataSource::toRecord);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    private static UserRecord toRecord(UserJpaEntity e) {
        return new UserRecord(e.getId(), e.getEmail(), e.getPasswordHash(), e.getFullName(), e.getRole());
    }
}
