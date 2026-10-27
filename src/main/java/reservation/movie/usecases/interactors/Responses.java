package reservation.movie.usecases.interactors;

import reservation.movie.entities.Auditorium;
import reservation.movie.entities.Movie;
import reservation.movie.entities.User;
import reservation.movie.usecases.ports.input.auditorium.AuditoriumResponse;
import reservation.movie.usecases.ports.input.auth.UserResponse;
import reservation.movie.usecases.ports.input.movie.MovieResponse;

/**
 * Entity -> response model mappings shared by several interactors.
 */
final class Responses {

    private Responses() {
    }

    static UserResponse user(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole().name());
    }

    static MovieResponse movie(Movie movie) {
        return new MovieResponse(movie.getId(), movie.getTitle(), movie.getDescription(), movie.getPosterUrl(),
                movie.getDurationMinutes(), movie.getGenres());
    }

    static AuditoriumResponse auditorium(Auditorium auditorium) {
        return new AuditoriumResponse(auditorium.getId(), auditorium.getName(), auditorium.getRows(),
                auditorium.getSeatsPerRow(), auditorium.capacity());
    }

    static int orZero(Integer value) {
        return value == null ? 0 : value;
    }
}
