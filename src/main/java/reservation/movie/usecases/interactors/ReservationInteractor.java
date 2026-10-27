package reservation.movie.usecases.interactors;

import reservation.movie.entities.Auditorium;
import reservation.movie.entities.DomainException;
import reservation.movie.entities.Reservation;
import reservation.movie.entities.Seat;
import reservation.movie.entities.Showtime;
import reservation.movie.usecases.exceptions.NotFoundException;
import reservation.movie.usecases.exceptions.SeatsAlreadyReservedException;
import reservation.movie.usecases.ports.input.reservation.IReservationInputPort;
import reservation.movie.usecases.ports.input.reservation.ReservationResponse;
import reservation.movie.usecases.ports.input.reservation.ReserveSeatsRequest;
import reservation.movie.usecases.ports.output.auditorium.IAuditoriumRepository;
import reservation.movie.usecases.ports.output.movie.IMovieRepository;
import reservation.movie.usecases.ports.output.reservation.IReservationRepository;
import reservation.movie.usecases.ports.output.showtime.IShowtimeRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class ReservationInteractor implements IReservationInputPort {

    private final IReservationRepository reservations;
    private final IShowtimeRepository showtimes;
    private final IAuditoriumRepository auditoriums;
    private final ReservationAssembler assembler;
    private final Clock clock;

    public ReservationInteractor(IReservationRepository reservations, IShowtimeRepository showtimes,
                                 IMovieRepository movies, IAuditoriumRepository auditoriums, Clock clock) {
        this.reservations = reservations;
        this.showtimes = showtimes;
        this.auditoriums = auditoriums;
        this.assembler = new ReservationAssembler(showtimes, movies, auditoriums, clock);
        this.clock = clock;
    }

    @Override
    public ReservationResponse reserve(Long userId, ReserveSeatsRequest request) {
        if (request.showtimeId() == null) {
            throw new DomainException("showtimeId is required");
        }
        Showtime showtime = findShowtime(request.showtimeId());
        Auditorium auditorium = auditoriums.findById(showtime.getAuditoriumId())
                .orElseThrow(() -> NotFoundException.of("Auditorium", showtime.getAuditoriumId()));
        List<Seat> seats = request.seats() == null ? List.of() : request.seats().stream().map(Seat::parse).toList();

        Reservation reservation = Reservation.create(userId, showtime, auditorium, seats, LocalDateTime.now(clock));

        // Friendly early check; the repository still guards against concurrent bookings.
        Set<Seat> taken = reservations.findReservedSeats(showtime.getId());
        List<String> clashes = reservation.getSeats().stream().filter(taken::contains).map(Seat::label).toList();
        if (!clashes.isEmpty()) {
            throw new SeatsAlreadyReservedException(clashes);
        }
        return assembler.toResponse(reservations.save(reservation));
    }

    @Override
    public List<ReservationResponse> listForUser(Long userId) {
        return assembler.toResponses(reservations.findByUserId(userId));
    }

    @Override
    public ReservationResponse cancel(Long userId, Long reservationId) {
        Reservation reservation = reservations.findById(reservationId)
                .filter(r -> r.isOwnedBy(userId))
                .orElseThrow(() -> NotFoundException.of("Reservation", reservationId));
        Showtime showtime = findShowtime(reservation.getShowtimeId());
        Reservation cancelled = reservation.cancel(showtime, LocalDateTime.now(clock));
        return assembler.toResponse(reservations.save(cancelled));
    }

    private Showtime findShowtime(Long showtimeId) {
        return showtimes.findById(showtimeId).orElseThrow(() -> NotFoundException.of("Showtime", showtimeId));
    }
}
