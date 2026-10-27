package reservation.movie.frameworks.db.datasource;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reservation.movie.adapters.gateways.showtime.ShowtimeDataSource;
import reservation.movie.adapters.gateways.showtime.ShowtimeRecord;
import reservation.movie.frameworks.db.entity.ShowtimeJpaEntity;
import reservation.movie.frameworks.db.repository.ShowtimeJpaRepository;
import reservation.movie.usecases.exceptions.ConflictException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class JpaShowtimeDataSource implements ShowtimeDataSource {

    private static final String NO_OVERLAP_CONSTRAINT = "ex_showtimes_no_overlap";

    private final ShowtimeJpaRepository repository;

    public JpaShowtimeDataSource(ShowtimeJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ShowtimeRecord save(ShowtimeRecord showtime) {
        ShowtimeJpaEntity entity = new ShowtimeJpaEntity();
        entity.setId(showtime.id());
        entity.setMovieId(showtime.movieId());
        entity.setAuditoriumId(showtime.auditoriumId());
        entity.setStartTime(showtime.startTime());
        entity.setEndTime(showtime.endTime());
        entity.setPrice(showtime.price());
        try {
            return toRecord(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException e) {
            if (ConstraintViolations.isViolationOf(e, NO_OVERLAP_CONSTRAINT)) {
                throw new ConflictException("The auditorium is already booked for an overlapping showtime");
            }
            throw e;
        }
    }

    @Override
    public Optional<ShowtimeRecord> findById(Long id) {
        return repository.findById(id).map(JpaShowtimeDataSource::toRecord);
    }

    @Override
    public List<ShowtimeRecord> findAllById(Collection<Long> ids) {
        return repository.findAllById(ids).stream().map(JpaShowtimeDataSource::toRecord).toList();
    }

    @Override
    public List<ShowtimeRecord> findStartingBetween(LocalDateTime from, LocalDateTime to) {
        return repository.findByStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(from, to).stream()
                .map(JpaShowtimeDataSource::toRecord).toList();
    }

    @Override
    public List<ShowtimeRecord> findOverlapping(Long auditoriumId, LocalDateTime start, LocalDateTime end) {
        return repository.findOverlapping(auditoriumId, start, end).stream()
                .map(JpaShowtimeDataSource::toRecord).toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    private static ShowtimeRecord toRecord(ShowtimeJpaEntity e) {
        return new ShowtimeRecord(e.getId(), e.getMovieId(), e.getAuditoriumId(), e.getStartTime(), e.getEndTime(),
                e.getPrice());
    }
}
