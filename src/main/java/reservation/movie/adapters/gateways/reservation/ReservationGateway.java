package reservation.movie.adapters.gateways.reservation;

import reservation.movie.entities.Reservation;
import reservation.movie.entities.ReservationStatus;
import reservation.movie.entities.Seat;
import reservation.movie.usecases.ports.output.reservation.IReservationRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ReservationGateway implements IReservationRepository {

    private final ReservationDataSource dataSource;

    public ReservationGateway(ReservationDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Reservation save(Reservation reservation) {
        return toEntity(dataSource.save(toRecord(reservation)));
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return dataSource.findById(id).map(ReservationGateway::toEntity);
    }

    @Override
    public List<Reservation> findByUserId(Long userId) {
        return toEntities(dataSource.findByUserId(userId));
    }

    @Override
    public List<Reservation> findByShowtimeId(Long showtimeId) {
        return toEntities(dataSource.findByShowtimeId(showtimeId));
    }

    @Override
    public List<Reservation> findByShowtimeStartBetween(LocalDateTime from, LocalDateTime to) {
        return toEntities(dataSource.findByShowtimeStartBetween(from, to));
    }

    @Override
    public Set<Seat> findReservedSeats(Long showtimeId) {
        return dataSource.findActiveSeats(showtimeId).stream().map(ReservationGateway::toSeat).collect(Collectors.toSet());
    }

    @Override
    public Map<Long, Integer> countReservedSeats(Collection<Long> showtimeIds) {
        return showtimeIds.isEmpty() ? Map.of() : dataSource.countActiveSeats(showtimeIds);
    }

    @Override
    public boolean existsActiveForShowtime(Long showtimeId) {
        return dataSource.existsActiveForShowtime(showtimeId);
    }

    @Override
    public boolean existsActiveForMovie(Long movieId) {
        return dataSource.existsActiveForMovie(movieId);
    }

    private static List<Reservation> toEntities(List<ReservationRecord> records) {
        return records.stream().map(ReservationGateway::toEntity).toList();
    }

    private static ReservationRecord toRecord(Reservation r) {
        List<SeatRecord> seats = r.getSeats().stream()
                .map(s -> new SeatRecord(String.valueOf(s.row()), s.number()))
                .toList();
        return new ReservationRecord(r.getId(), r.getUserId(), r.getShowtimeId(), seats, r.getTotalPrice(),
                r.getStatus().name(), r.getCreatedAt(), r.getCancelledAt());
    }

    private static Reservation toEntity(ReservationRecord record) {
        return new Reservation(record.id(), record.userId(), record.showtimeId(),
                record.seats().stream().map(ReservationGateway::toSeat).toList(), record.totalPrice(),
                ReservationStatus.valueOf(record.status()), record.createdAt(), record.cancelledAt());
    }

    private static Seat toSeat(SeatRecord record) {
        return new Seat(record.row().charAt(0), record.number());
    }
}
