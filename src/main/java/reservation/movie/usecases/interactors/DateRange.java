package reservation.movie.usecases.interactors;

import reservation.movie.entities.DomainException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * An inclusive range of calendar days, converted to the half-open timestamp range [start, end).
 */
record DateRange(LocalDate from, LocalDate to) {

    private static final int MAX_DAYS = 366;

    DateRange {
        if (from.isAfter(to)) {
            throw new DomainException("'from' must not be after 'to'");
        }
        if (from.plusDays(MAX_DAYS).isBefore(to)) {
            throw new DomainException("Date range must not exceed " + MAX_DAYS + " days");
        }
    }

    static DateRange of(LocalDate from, LocalDate to, LocalDate defaultFrom, LocalDate defaultTo) {
        return new DateRange(from == null ? defaultFrom : from, to == null ? defaultTo : to);
    }

    LocalDateTime start() {
        return from.atStartOfDay();
    }

    LocalDateTime end() {
        return to.plusDays(1).atStartOfDay();
    }
}
