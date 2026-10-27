package reservation.movie.adapters.gateways.auditorium;

import java.util.List;
import java.util.Optional;

public interface AuditoriumDataSource {

    /** Must throw {@code ConflictException} when the name is already taken (ignoring case). */
    AuditoriumRecord save(AuditoriumRecord auditorium);

    Optional<AuditoriumRecord> findById(Long id);

    List<AuditoriumRecord> findAll();

    boolean existsByName(String name);
}
