package reservation.movie.adapters.gateways.auditorium;

import java.util.List;
import java.util.Optional;

public interface AuditoriumDataSource {

    AuditoriumRecord save(AuditoriumRecord auditorium);

    Optional<AuditoriumRecord> findById(Long id);

    List<AuditoriumRecord> findAll();

    boolean existsByName(String name);
}
