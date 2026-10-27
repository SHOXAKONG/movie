package reservation.movie.usecases.ports.output.auditorium;

import reservation.movie.entities.Auditorium;

import java.util.List;
import java.util.Optional;

public interface IAuditoriumRepository {

    Auditorium save(Auditorium auditorium);

    Optional<Auditorium> findById(Long id);

    List<Auditorium> findAll();

    boolean existsByName(String name);
}
