package reservation.movie.usecases.interactors;

import reservation.movie.entities.Auditorium;
import reservation.movie.entities.DomainException;
import reservation.movie.entities.Movie;
import reservation.movie.entities.Seat;
import reservation.movie.entities.Showtime;
import reservation.movie.usecases.exceptions.ConflictException;
import reservation.movie.usecases.exceptions.NotFoundException;
import reservation.movie.usecases.ports.input.showtime.IShowtimeInputPort;
import reservation.movie.usecases.ports.input.showtime.MovieScheduleResponse;
import reservation.movie.usecases.ports.input.showtime.ScheduleShowtimeRequest;
import reservation.movie.usecases.ports.input.showtime.SeatMapResponse;
import reservation.movie.usecases.ports.input.showtime.ShowtimeResponse;
import reservation.movie.usecases.ports.output.auditorium.IAuditoriumRepository;
import reservation.movie.usecases.ports.output.movie.IMovieRepository;
import reservation.movie.usecases.ports.output.reservation.IReservationRepository;
import reservation.movie.usecases.ports.output.showtime.IShowtimeRepository;
import reservation.movie.usecases.ports.output.transaction.ITransactionRunner;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShowtimeInteractor implements IShowtimeInputPort {

    private final IShowtimeRepository showtimes;
    private final IMovieRepository movies;
    private final IAuditoriumRepository auditoriums;
    private final IReservationRepository reservations;
    private final ITransactionRunner transactions;
    private final Clock clock;

    public ShowtimeInteractor(IShowtimeRepository showtimes, IMovieRepository movies,
                              IAuditoriumRepository auditoriums, IReservationRepository reservations,
                              ITransactionRunner transactions, Clock clock) {
        this.showtimes = showtimes;
        this.movies = movies;
        this.auditoriums = auditoriums;
        this.reservations = reservations;
        this.transactions = transactions;
        this.clock = clock;
    }

    @Override
    public ShowtimeResponse schedule(ScheduleShowtimeRequest request) {
        if (request.movieId() == null || request.auditoriumId() == null) {
            throw new DomainException("movieId and auditoriumId are required");
        }
        Movie movie = movies.findById(request.movieId())
                .orElseThrow(() -> NotFoundException.of("Movie", request.movieId()));
        Auditorium auditorium = findAuditorium(request.auditoriumId());
        Showtime showtime = Showtime.schedule(movie, auditorium, request.startTime(), request.price(),
                LocalDateTime.now(clock));

        List<Showtime> clashes = showtimes.findOverlapping(
                auditorium.getId(), showtime.getStartTime(), showtime.getEndTime());
        if (!clashes.isEmpty()) {
            Showtime clash = clashes.getFirst();
            throw new ConflictException("%s is busy from %s to %s (showtime %d)".formatted(
                    auditorium.getName(), clash.getStartTime(), clash.getEndTime(), clash.getId()));
        }
        Showtime saved = showtimes.save(showtime);
        return toResponse(saved, movie, auditorium, 0);
    }

    @Override
    public void delete(Long showtimeId) {
        transactions.run(() -> {
            find(showtimeId);
            if (reservations.existsActiveForShowtime(showtimeId)) {
                throw new ConflictException("Showtime has confirmed reservations and cannot be deleted");
            }
            reservations.deleteCancelledForShowtime(showtimeId);
            // The database still refuses if a booking slipped in after the check above.
            showtimes.deleteById(showtimeId);
        });
    }

    @Override
    public ShowtimeResponse get(Long showtimeId) {
        Showtime showtime = find(showtimeId);
        Movie movie = movies.findById(showtime.getMovieId())
                .orElseThrow(() -> NotFoundException.of("Movie", showtime.getMovieId()));
        int reserved = reservations.countReservedSeats(List.of(showtimeId)).getOrDefault(showtimeId, 0);
        return toResponse(showtime, movie, findAuditorium(showtime.getAuditoriumId()), reserved);
    }

    @Override
    public List<MovieScheduleResponse> listForDate(LocalDate date) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate day = date == null ? now.toLocalDate() : date;
        List<Showtime> upcoming = showtimes.findStartingBetween(day.atStartOfDay(), day.plusDays(1).atStartOfDay())
                .stream()
                .filter(s -> !s.hasStarted(now))
                .toList();
        if (upcoming.isEmpty()) {
            return List.of();
        }

        Map<Long, Movie> moviesById = movies.findAllById(upcoming.stream().map(Showtime::getMovieId).distinct().toList())
                .stream().collect(Collectors.toMap(Movie::getId, Function.identity()));
        Map<Long, Auditorium> auditoriumsById = auditoriums.findAll().stream()
                .collect(Collectors.toMap(Auditorium::getId, Function.identity()));
        Map<Long, Integer> reserved = reservations.countReservedSeats(upcoming.stream().map(Showtime::getId).toList());

        // Movies are ordered by their first showtime of the day.
        Map<Long, List<ShowtimeResponse>> byMovie = new LinkedHashMap<>();
        for (Showtime showtime : upcoming) {
            Movie movie = moviesById.get(showtime.getMovieId());
            Auditorium auditorium = auditoriumsById.get(showtime.getAuditoriumId());
            byMovie.computeIfAbsent(movie.getId(), id -> new ArrayList<>())
                    .add(toResponse(showtime, movie, auditorium, reserved.getOrDefault(showtime.getId(), 0)));
        }
        return byMovie.entrySet().stream()
                .map(e -> new MovieScheduleResponse(Responses.movie(moviesById.get(e.getKey())), e.getValue()))
                .toList();
    }

    @Override
    public SeatMapResponse seatMap(Long showtimeId) {
        Showtime showtime = find(showtimeId);
        Auditorium auditorium = findAuditorium(showtime.getAuditoriumId());
        Set<Seat> taken = reservations.findReservedSeats(showtimeId);
        List<SeatMapResponse.SeatStatus> seats = auditorium.allSeats().stream()
                .map(seat -> new SeatMapResponse.SeatStatus(
                        seat.label(), String.valueOf(seat.row()), seat.number(), !taken.contains(seat)))
                .toList();
        return new SeatMapResponse(showtimeId, auditorium.getRows(), auditorium.getSeatsPerRow(),
                auditorium.capacity() - taken.size(), seats);
    }

    private Showtime find(Long showtimeId) {
        return showtimes.findById(showtimeId).orElseThrow(() -> NotFoundException.of("Showtime", showtimeId));
    }

    private Auditorium findAuditorium(Long auditoriumId) {
        return auditoriums.findById(auditoriumId).orElseThrow(() -> NotFoundException.of("Auditorium", auditoriumId));
    }

    private static ShowtimeResponse toResponse(Showtime showtime, Movie movie, Auditorium auditorium, int reserved) {
        return new ShowtimeResponse(showtime.getId(), movie.getId(), movie.getTitle(), auditorium.getId(),
                auditorium.getName(), showtime.getStartTime(), showtime.getEndTime(), showtime.getPrice(),
                auditorium.capacity(), auditorium.capacity() - reserved);
    }
}
