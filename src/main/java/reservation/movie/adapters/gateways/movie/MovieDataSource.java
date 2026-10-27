package reservation.movie.adapters.gateways.movie;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * What the gateway needs from a database. Implemented in {@code frameworks.db}, so the
 * adapter layer never imports JPA or Spring Data.
 */
public interface MovieDataSource {

    MovieRecord save(MovieRecord movie);

    Optional<MovieRecord> findById(Long id);

    List<MovieRecord> findAll();

    List<MovieRecord> findByGenre(String genre);

    List<MovieRecord> findAllById(Collection<Long> ids);

    void deleteById(Long id);
}
