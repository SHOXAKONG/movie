package reservation.movie.frameworks.db.datasource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reservation.movie.adapters.gateways.movie.MovieDataSource;
import reservation.movie.adapters.gateways.movie.MovieRecord;
import reservation.movie.frameworks.db.entity.MovieJpaEntity;
import reservation.movie.frameworks.db.repository.MovieJpaRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class JpaMovieDataSource implements MovieDataSource {

    private final MovieJpaRepository repository;

    public JpaMovieDataSource(MovieJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public MovieRecord save(MovieRecord movie) {
        MovieJpaEntity entity = movie.id() == null
                ? new MovieJpaEntity()
                : repository.findById(movie.id()).orElseGet(MovieJpaEntity::new);
        entity.setTitle(movie.title());
        entity.setDescription(movie.description());
        entity.setPosterUrl(movie.posterUrl());
        entity.setDurationMinutes(movie.durationMinutes());
        entity.getGenres().clear();
        entity.getGenres().addAll(movie.genres());
        return toRecord(repository.save(entity));
    }

    @Override
    public Optional<MovieRecord> findById(Long id) {
        return repository.findById(id).map(JpaMovieDataSource::toRecord);
    }

    @Override
    public List<MovieRecord> findAll() {
        return repository.findAllByOrderByTitleAsc().stream().map(JpaMovieDataSource::toRecord).toList();
    }

    @Override
    public List<MovieRecord> findByGenre(String genre) {
        return repository.findByGenre(genre).stream().map(JpaMovieDataSource::toRecord).toList();
    }

    @Override
    public List<MovieRecord> findAllById(Collection<Long> ids) {
        return repository.findAllById(ids).stream().map(JpaMovieDataSource::toRecord).toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private static MovieRecord toRecord(MovieJpaEntity e) {
        return new MovieRecord(e.getId(), e.getTitle(), e.getDescription(), e.getPosterUrl(), e.getDurationMinutes(),
                List.copyOf(new ArrayList<>(new LinkedHashSet<>(e.getGenres()))));
    }
}
