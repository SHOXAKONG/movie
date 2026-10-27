package reservation.movie.usecases.exceptions;

/**
 * The request is valid but clashes with the current state (taken seats, overlapping showtimes, ...).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
