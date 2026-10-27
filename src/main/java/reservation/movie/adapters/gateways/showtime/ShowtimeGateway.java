package reservation.movie.adapters.gateways.showtime;

import reservation.movie.entities.Showtime;
import reservation.movie.usecases.ports.output.showtime.IShowtimeRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ShowtimeGateway implements IShowtimeRepository {

    private final ShowtimeDataSource dataSource;

    public ShowtimeGateway(ShowtimeDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Showtime save(Showtime showtime) {
        return toEntity(dataSource.save(new ShowtimeRecord(showtime.getId(), showtime.getMovieId(),
                showtime.getAuditoriumId(), showtime.getStartTime(), showtime.getEndTime(), showtime.getPrice())));
    }

    @Override
    public Optional<Showtime> findById(Long id) {
        return dataSource.findById(id).map(ShowtimeGateway::toEntity);
    }

    @Override
    public List<Showtime> findAllById(Collection<Long> ids) {
        return ids.isEmpty() ? List.of() : toEntities(dataSource.findAllById(ids));
    }

    @Override
    public List<Showtime> findStartingBetween(LocalDateTime from, LocalDateTime to) {
        return toEntities(dataSource.findStartingBetween(from, to));
    }

    @Override
    public List<Showtime> findOverlapping(Long auditoriumId, LocalDateTime start, LocalDateTime end) {
        return toEntities(dataSource.findOverlapping(auditoriumId, start, end));
    }

    @Override
    public void deleteById(Long id) {
        dataSource.deleteById(id);
    }

    private static List<Showtime> toEntities(List<ShowtimeRecord> records) {
        return records.stream().map(ShowtimeGateway::toEntity).toList();
    }

    private static Showtime toEntity(ShowtimeRecord record) {
        return new Showtime(record.id(), record.movieId(), record.auditoriumId(), record.startTime(),
                record.endTime(), record.price());
    }
}
