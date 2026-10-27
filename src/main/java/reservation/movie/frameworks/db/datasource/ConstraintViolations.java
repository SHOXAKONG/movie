package reservation.movie.frameworks.db.datasource;

import org.springframework.dao.DataIntegrityViolationException;

final class ConstraintViolations {

    /** Raised when a showtime (or its movie) is deleted while reservations still reference it. */
    static final String RESERVATIONS_REFERENCE_SHOWTIME = "fk_reservations_showtime";

    private ConstraintViolations() {
    }

    /** True when the violation was raised by the named database constraint or index. */
    static boolean isViolationOf(DataIntegrityViolationException e, String constraintName) {
        Throwable cause = e;
        while (cause != null) {
            if (cause.getMessage() != null && cause.getMessage().contains(constraintName)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
