package reservation.movie.frameworks.db.datasource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reservation.movie.adapters.gateways.auditorium.AuditoriumDataSource;
import reservation.movie.adapters.gateways.auditorium.AuditoriumRecord;
import reservation.movie.frameworks.db.entity.AuditoriumJpaEntity;
import reservation.movie.frameworks.db.repository.AuditoriumJpaRepository;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class JpaAuditoriumDataSource implements AuditoriumDataSource {

    private final AuditoriumJpaRepository repository;

    public JpaAuditoriumDataSource(AuditoriumJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public AuditoriumRecord save(AuditoriumRecord auditorium) {
        AuditoriumJpaEntity entity = new AuditoriumJpaEntity();
        entity.setId(auditorium.id());
        entity.setName(auditorium.name());
        entity.setRowCount(auditorium.rows());
        entity.setSeatsPerRow(auditorium.seatsPerRow());
        return toRecord(repository.save(entity));
    }

    @Override
    public Optional<AuditoriumRecord> findById(Long id) {
        return repository.findById(id).map(JpaAuditoriumDataSource::toRecord);
    }

    @Override
    public List<AuditoriumRecord> findAll() {
        return repository.findAllByOrderByNameAsc().stream().map(JpaAuditoriumDataSource::toRecord).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    private static AuditoriumRecord toRecord(AuditoriumJpaEntity e) {
        return new AuditoriumRecord(e.getId(), e.getName(), e.getRowCount(), e.getSeatsPerRow());
    }
}
