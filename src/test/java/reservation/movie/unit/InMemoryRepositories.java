package reservation.movie.unit;

import reservation.movie.entities.Auditorium;
import reservation.movie.entities.Movie;
import reservation.movie.entities.Reservation;
import reservation.movie.entities.Seat;
import reservation.movie.entities.Showtime;
import reservation.movie.entities.User;
import reservation.movie.usecases.ports.output.auditorium.IAuditoriumRepository;
import reservation.movie.usecases.ports.output.auth.IUserRepository;
import reservation.movie.usecases.ports.output.movie.IMovieRepository;
import reservation.movie.usecases.ports.output.reservation.IReservationRepository;
import reservation.movie.usecases.ports.output.showtime.IShowtimeRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Hand-written fakes for the output ports so interactors can be tested without Spring or a database.
 */
final class InMemoryRepositories {

    private InMemoryRepositories() {
    }

    static class Users implements IUserRepository {
        final Map<Long, User> rows = new LinkedHashMap<>();
        private final AtomicLong ids = new AtomicLong();

        @Override
        public User save(User u) {
            User saved = new User(u.getId() == null ? ids.incrementAndGet() : u.getId(), u.getEmail(),
                    u.getPasswordHash(), u.getFullName(), u.getRole());
            rows.put(saved.getId(), saved);
            return saved;
        }

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(rows.get(id));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return rows.values().stream().filter(u -> u.getEmail().equals(email)).findFirst();
        }

        @Override
        public boolean existsByEmail(String email) {
            return findByEmail(email).isPresent();
        }
    }

    static class Movies implements IMovieRepository {
        final Map<Long, Movie> rows = new LinkedHashMap<>();
        private final AtomicLong ids = new AtomicLong();

        @Override
        public Movie save(Movie movie) {
            Movie saved = movie.getId() == null ? movie.withId(ids.incrementAndGet()) : movie;
            rows.put(saved.getId(), saved);
            return saved;
        }

        @Override
        public Optional<Movie> findById(Long id) {
            return Optional.ofNullable(rows.get(id));
        }

        @Override
        public List<Movie> findAll() {
            return List.copyOf(rows.values());
        }

        @Override
        public List<Movie> findByGenre(String genre) {
            return rows.values().stream().filter(m -> m.hasGenre(genre)).toList();
        }

        @Override
        public List<Movie> findAllById(Collection<Long> ids) {
            return ids.stream().map(rows::get).filter(Objects::nonNull).toList();
        }

        @Override
        public void deleteById(Long id) {
            rows.remove(id);
        }
    }

    static class Auditoriums implements IAuditoriumRepository {
        final Map<Long, Auditorium> rows = new LinkedHashMap<>();
        private final AtomicLong ids = new AtomicLong();

        @Override
        public Auditorium save(Auditorium a) {
            Auditorium saved = new Auditorium(a.getId() == null ? ids.incrementAndGet() : a.getId(), a.getName(),
                    a.getRows(), a.getSeatsPerRow());
            rows.put(saved.getId(), saved);
            return saved;
        }

        @Override
        public Optional<Auditorium> findById(Long id) {
            return Optional.ofNullable(rows.get(id));
        }

        @Override
        public List<Auditorium> findAll() {
            return List.copyOf(rows.values());
        }

        @Override
        public boolean existsByName(String name) {
            return rows.values().stream().anyMatch(a -> a.getName().equalsIgnoreCase(name));
        }
    }

    static class Showtimes implements IShowtimeRepository {
        final Map<Long, Showtime> rows = new LinkedHashMap<>();
        private final AtomicLong ids = new AtomicLong();

        @Override
        public Showtime save(Showtime s) {
            Showtime saved = new Showtime(s.getId() == null ? ids.incrementAndGet() : s.getId(), s.getMovieId(),
                    s.getAuditoriumId(), s.getStartTime(), s.getEndTime(), s.getPrice());
            rows.put(saved.getId(), saved);
            return saved;
        }

        @Override
        public Optional<Showtime> findById(Long id) {
            return Optional.ofNullable(rows.get(id));
        }

        @Override
        public List<Showtime> findAllById(Collection<Long> ids) {
            return ids.stream().map(rows::get).filter(Objects::nonNull).toList();
        }

        @Override
        public List<Showtime> findStartingBetween(LocalDateTime from, LocalDateTime to) {
            return rows.values().stream()
                    .filter(s -> !s.getStartTime().isBefore(from) && s.getStartTime().isBefore(to))
                    .toList();
        }

        @Override
        public List<Showtime> findOverlapping(Long auditoriumId, LocalDateTime start, LocalDateTime end) {
            return rows.values().stream()
                    .filter(s -> s.getAuditoriumId().equals(auditoriumId)
                            && s.getStartTime().isBefore(end) && s.getEndTime().isAfter(start))
                    .toList();
        }

        @Override
        public boolean existsUnfinishedForMovie(Long movieId, LocalDateTime now) {
            return rows.values().stream().anyMatch(s -> s.getMovieId().equals(movieId) && s.getEndTime().isAfter(now));
        }

        @Override
        public void deleteById(Long id) {
            rows.remove(id);
        }
    }

    static class Reservations implements IReservationRepository {
        final Map<Long, Reservation> rows = new LinkedHashMap<>();
        private final AtomicLong ids = new AtomicLong();
        private final Showtimes showtimes;

        Reservations(Showtimes showtimes) {
            this.showtimes = showtimes;
        }

        @Override
        public Reservation save(Reservation r) {
            Reservation saved = new Reservation(r.getId() == null ? ids.incrementAndGet() : r.getId(), r.getUserId(),
                    r.getShowtimeId(), r.getSeats(), r.getTotalPrice(), r.getStatus(), r.getCreatedAt(),
                    r.getCancelledAt());
            rows.put(saved.getId(), saved);
            return saved;
        }

        @Override
        public Optional<Reservation> findById(Long id) {
            return Optional.ofNullable(rows.get(id));
        }

        @Override
        public List<Reservation> findByUserId(Long userId) {
            return rows.values().stream().filter(r -> r.getUserId().equals(userId)).toList();
        }

        @Override
        public List<Reservation> findByShowtimeId(Long showtimeId) {
            return rows.values().stream().filter(r -> r.getShowtimeId().equals(showtimeId)).toList();
        }

        @Override
        public List<Reservation> findByShowtimeStartBetween(LocalDateTime from, LocalDateTime to) {
            Set<Long> ids = showtimes.findStartingBetween(from, to).stream().map(Showtime::getId)
                    .collect(Collectors.toSet());
            return rows.values().stream().filter(r -> ids.contains(r.getShowtimeId())).toList();
        }

        @Override
        public Set<Seat> findReservedSeats(Long showtimeId) {
            return rows.values().stream()
                    .filter(r -> r.isActive() && r.getShowtimeId().equals(showtimeId))
                    .flatMap(r -> r.getSeats().stream())
                    .collect(Collectors.toSet());
        }

        @Override
        public Map<Long, Integer> countReservedSeats(Collection<Long> showtimeIds) {
            Map<Long, Integer> counts = new HashMap<>();
            showtimeIds.forEach(id -> counts.put(id, findReservedSeats(id).size()));
            return counts;
        }

        @Override
        public boolean existsActiveForShowtime(Long showtimeId) {
            return rows.values().stream().anyMatch(r -> r.isActive() && r.getShowtimeId().equals(showtimeId));
        }

        @Override
        public boolean existsActiveForMovie(Long movieId) {
            return rows.values().stream().anyMatch(r -> r.isActive()
                    && showtimes.findById(r.getShowtimeId()).map(s -> s.getMovieId().equals(movieId)).orElse(false));
        }

        @Override
        public void deleteCancelledForShowtime(Long showtimeId) {
            rows.values().removeIf(r -> !r.isActive() && r.getShowtimeId().equals(showtimeId));
        }

        @Override
        public void deleteCancelledForMovie(Long movieId) {
            rows.values().removeIf(r -> !r.isActive()
                    && showtimes.findById(r.getShowtimeId()).map(s -> s.getMovieId().equals(movieId)).orElse(false));
        }
    }
}
