package reservation.movie.entities;

/**
 * Thrown when an enterprise business rule is violated.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
