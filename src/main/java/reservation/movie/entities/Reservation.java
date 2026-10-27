package reservation.movie.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

public class Reservation {

    public static final int MAX_SEATS_PER_RESERVATION = 10;

    private final Long id;
    private final Long userId;
    private final Long showtimeId;
    private final List<Seat> seats;
    private final BigDecimal totalPrice;
    private final ReservationStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime cancelledAt;

    public Reservation(Long id, Long userId, Long showtimeId, Collection<Seat> seats, BigDecimal totalPrice,
                       ReservationStatus status, LocalDateTime createdAt, LocalDateTime cancelledAt) {
        if (userId == null || showtimeId == null) {
            throw new DomainException("A reservation needs a user and a showtime");
        }
        if (seats == null || seats.isEmpty()) {
            throw new DomainException("Select at least one seat");
        }
        this.id = id;
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.seats = List.copyOf(new TreeSet<>(seats));
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.cancelledAt = cancelledAt;
    }

    /**
     * Applies every booking rule that does not need to know about other reservations.
     * Seat availability is checked by the use case and guaranteed by the database.
     */
    public static Reservation create(Long userId, Showtime showtime, Auditorium auditorium, List<Seat> seats,
                                     LocalDateTime now) {
        if (showtime.hasStarted(now)) {
            throw new DomainException("This showtime has already started");
        }
        if (seats == null || seats.isEmpty()) {
            throw new DomainException("Select at least one seat");
        }
        if (seats.size() > MAX_SEATS_PER_RESERVATION) {
            throw new DomainException("At most " + MAX_SEATS_PER_RESERVATION + " seats can be reserved at once");
        }
        if (new TreeSet<>(seats).size() != seats.size()) {
            throw new DomainException("The same seat was selected more than once");
        }
        for (Seat seat : seats) {
            if (!auditorium.contains(seat)) {
                throw new DomainException("Seat " + seat + " does not exist in " + auditorium.getName());
            }
        }
        BigDecimal total = showtime.getPrice().multiply(BigDecimal.valueOf(seats.size()));
        return new Reservation(null, userId, showtime.getId(), seats, total, ReservationStatus.CONFIRMED, now, null);
    }

    /**
     * Only confirmed reservations for showtimes that have not started yet can be cancelled.
     */
    public Reservation cancel(Showtime showtime, LocalDateTime now) {
        if (status == ReservationStatus.CANCELLED) {
            throw new DomainException("Reservation is already cancelled");
        }
        if (showtime.hasStarted(now)) {
            throw new DomainException("Only upcoming reservations can be cancelled");
        }
        return new Reservation(id, userId, showtimeId, seats, totalPrice, ReservationStatus.CANCELLED, createdAt, now);
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean isActive() {
        return status == ReservationStatus.CONFIRMED;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
}
