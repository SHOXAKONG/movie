package reservation.movie.usecases.ports.output.auditorium;

import reservation.movie.entities.Auditorium;

import java.util.List;
import java.util.Optional;

public interface IAuditoriumRepository {

    /** Must throw {@code ConflictException} if the name is already taken (ignoring case). */
    Auditorium save(Auditorium auditorium);

    Optional<Auditorium> findById(Long id);

    List<Auditorium> findAll();

    boolean existsByName(String name);
}
