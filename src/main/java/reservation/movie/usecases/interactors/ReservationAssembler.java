package reservation.movie.usecases.interactors;

import reservation.movie.entities.Auditorium;
import reservation.movie.entities.Movie;
import reservation.movie.entities.Reservation;
import reservation.movie.entities.Seat;
import reservation.movie.entities.Showtime;
import reservation.movie.usecases.ports.input.reservation.ReservationResponse;
import reservation.movie.usecases.ports.output.auditorium.IAuditoriumRepository;
import reservation.movie.usecases.ports.output.movie.IMovieRepository;
import reservation.movie.usecases.ports.output.showtime.IShowtimeRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds reservation responses, loading related showtimes, movies and auditoriums in batches.
 */
class ReservationAssembler {

    private final IShowtimeRepository showtimes;
    private final IMovieRepository movies;
    private final IAuditoriumRepository auditoriums;
    private final Clock clock;

    ReservationAssembler(IShowtimeRepository showtimes, IMovieRepository movies, IAuditoriumRepository auditoriums,
                         Clock clock) {
        this.showtimes = showtimes;
        this.movies = movies;
        this.auditoriums = auditoriums;
        this.clock = clock;
    }

    ReservationResponse toResponse(Reservation reservation) {
        return toResponses(List.of(reservation)).getFirst();
    }

    List<ReservationResponse> toResponses(List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            return List.of();
        }
        Map<Long, Showtime> showtimesById = showtimes.findAllById(
                        reservations.stream().map(Reservation::getShowtimeId).distinct().toList())
                .stream().collect(Collectors.toMap(Showtime::getId, Function.identity()));
        Map<Long, Movie> moviesById = movies.findAllById(
                        showtimesById.values().stream().map(Showtime::getMovieId).distinct().toList())
                .stream().collect(Collectors.toMap(Movie::getId, Function.identity()));
        Map<Long, Auditorium> auditoriumsById = auditoriums.findAll().stream()
                .collect(Collectors.toMap(Auditorium::getId, Function.identity()));
        LocalDateTime now = LocalDateTime.now(clock);

        return reservations.stream().map(r -> {
            Showtime showtime = showtimesById.get(r.getShowtimeId());
            Movie movie = moviesById.get(showtime.getMovieId());
            Auditorium auditorium = auditoriumsById.get(showtime.getAuditoriumId());
            return new ReservationResponse(r.getId(), r.getUserId(), showtime.getId(), movie.getTitle(),
                    auditorium.getName(), showtime.getStartTime(), r.getSeats().stream().map(Seat::label).toList(),
                    r.getTotalPrice(), r.getStatus().name(), r.getCreatedAt(), r.getCancelledAt(),
                    r.isActive() && !showtime.hasStarted(now));
        }).toList();
    }
}
