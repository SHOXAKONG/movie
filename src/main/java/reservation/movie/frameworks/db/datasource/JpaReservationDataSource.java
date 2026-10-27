package reservation.movie.frameworks.db.datasource;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reservation.movie.adapters.gateways.reservation.ReservationDataSource;
import reservation.movie.adapters.gateways.reservation.ReservationRecord;
import reservation.movie.adapters.gateways.reservation.SeatRecord;
import reservation.movie.frameworks.db.entity.ReservationJpaEntity;
import reservation.movie.frameworks.db.entity.ReservationSeatJpaEntity;
import reservation.movie.frameworks.db.repository.ReservationJpaRepository;
import reservation.movie.usecases.exceptions.NotFoundException;
import reservation.movie.usecases.exceptions.SeatsAlreadyReservedException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class JpaReservationDataSource implements ReservationDataSource {

    private static final String CONFIRMED = "CONFIRMED";
    private static final String NO_DOUBLE_BOOKING_INDEX = "ux_reservation_seats_active";

    private final ReservationJpaRepository repository;

    public JpaReservationDataSource(ReservationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ReservationRecord save(ReservationRecord record) {
        ReservationJpaEntity entity = record.id() == null ? newEntity(record) : existing(record);
        try {
            return toRecord(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException e) {
            if (ConstraintViolations.isViolationOf(e, NO_DOUBLE_BOOKING_INDEX)) {
                throw new SeatsAlreadyReservedException();
            }
            throw e;
        }
    }

    private static ReservationJpaEntity newEntity(ReservationRecord record) {
        ReservationJpaEntity entity = new ReservationJpaEntity();
        entity.setUserId(record.userId());
        entity.setShowtimeId(record.showtimeId());
        entity.setTotalPrice(record.totalPrice());
        entity.setStatus(record.status());
        entity.setCreatedAt(record.createdAt());
        entity.setCancelledAt(record.cancelledAt());
        for (SeatRecord seat : record.seats()) {
            ReservationSeatJpaEntity seatEntity = new ReservationSeatJpaEntity();
            seatEntity.setReservation(entity);
            seatEntity.setShowtimeId(record.showtimeId());
            seatEntity.setSeatRow(seat.row());
            seatEntity.setSeatNumber(seat.number());
            seatEntity.setActive(CONFIRMED.equals(record.status()));
            entity.getSeats().add(seatEntity);
        }
        return entity;
    }

    /** Seats never change after booking; only the status does, and a cancellation frees the seats. */
    private ReservationJpaEntity existing(ReservationRecord record) {
        ReservationJpaEntity entity = repository.findById(record.id())
                .orElseThrow(() -> NotFoundException.of("Reservation", record.id()));
        entity.setStatus(record.status());
        entity.setCancelledAt(record.cancelledAt());
        boolean active = CONFIRMED.equals(record.status());
        entity.getSeats().forEach(seat -> seat.setActive(active));
        return entity;
    }

    @Override
    public Optional<ReservationRecord> findById(Long id) {
        return repository.findById(id).map(JpaReservationDataSource::toRecord);
    }

    @Override
    public List<ReservationRecord> findByUserId(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(JpaReservationDataSource::toRecord).toList();
    }

    @Override
    public List<ReservationRecord> findByShowtimeId(Long showtimeId) {
        return repository.findByShowtimeIdOrderByCreatedAtAsc(showtimeId).stream()
                .map(JpaReservationDataSource::toRecord).toList();
    }

    @Override
    public List<ReservationRecord> findByShowtimeStartBetween(LocalDateTime from, LocalDateTime to) {
        return repository.findByShowtimeStartBetween(from, to).stream()
                .map(JpaReservationDataSource::toRecord).toList();
    }

    @Override
    public List<SeatRecord> findActiveSeats(Long showtimeId) {
        return repository.findActiveSeats(showtimeId).stream()
                .map(s -> new SeatRecord(s.getSeatRow(), s.getSeatNumber())).toList();
    }

    @Override
    public Map<Long, Integer> countActiveSeats(Collection<Long> showtimeIds) {
        Map<Long, Integer> counts = new HashMap<>();
        for (Object[] row : repository.countActiveSeats(showtimeIds)) {
            counts.put((Long) row[0], ((Number) row[1]).intValue());
        }
        return counts;
    }

    @Override
    public boolean existsActiveForShowtime(Long showtimeId) {
        return repository.existsByShowtimeIdAndStatus(showtimeId, CONFIRMED);
    }

    @Override
    public boolean existsActiveForMovie(Long movieId) {
        return repository.existsConfirmedForMovie(movieId);
    }

    private static ReservationRecord toRecord(ReservationJpaEntity e) {
        List<SeatRecord> seats = e.getSeats().stream()
                .map(s -> new SeatRecord(s.getSeatRow(), s.getSeatNumber()))
                .toList();
        return new ReservationRecord(e.getId(), e.getUserId(), e.getShowtimeId(), seats, e.getTotalPrice(),
                e.getStatus(), e.getCreatedAt(), e.getCancelledAt());
    }
}
