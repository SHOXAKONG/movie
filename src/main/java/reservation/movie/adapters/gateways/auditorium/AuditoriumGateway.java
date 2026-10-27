package reservation.movie.adapters.gateways.auditorium;

import reservation.movie.entities.Auditorium;
import reservation.movie.usecases.ports.output.auditorium.IAuditoriumRepository;

import java.util.List;
import java.util.Optional;

public class AuditoriumGateway implements IAuditoriumRepository {

    private final AuditoriumDataSource dataSource;

    public AuditoriumGateway(AuditoriumDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Auditorium save(Auditorium auditorium) {
        return toEntity(dataSource.save(new AuditoriumRecord(auditorium.getId(), auditorium.getName(),
                auditorium.getRows(), auditorium.getSeatsPerRow())));
    }

    @Override
    public Optional<Auditorium> findById(Long id) {
        return dataSource.findById(id).map(AuditoriumGateway::toEntity);
    }

    @Override
    public List<Auditorium> findAll() {
        return dataSource.findAll().stream().map(AuditoriumGateway::toEntity).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return dataSource.existsByName(name);
    }

    private static Auditorium toEntity(AuditoriumRecord record) {
        return new Auditorium(record.id(), record.name(), record.rows(), record.seatsPerRow());
    }
}
