package reservation.movie.frameworks.db.datasource;

import org.springframework.dao.DataIntegrityViolationException;

final class ConstraintViolations {

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
