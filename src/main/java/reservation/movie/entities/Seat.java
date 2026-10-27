package reservation.movie.entities;

import java.util.Locale;

/**
 * A seat position: a row letter (A, B, ...) and a 1-based number within the row, e.g. "C7".
 */
public record Seat(char row, int number) implements Comparable<Seat> {

    public Seat {
        if (row < 'A' || row > 'Z') {
            throw new DomainException("Seat row must be a letter A-Z");
        }
        if (number < 1) {
            throw new DomainException("Seat number must be positive");
        }
    }

    public static Seat parse(String label) {
        if (label == null || !label.strip().matches("(?i)^[A-Z][1-9][0-9]*$")) {
            throw new DomainException("Invalid seat '" + label + "', expected a label like A1");
        }
        String normalized = label.strip().toUpperCase(Locale.ROOT);
        return new Seat(normalized.charAt(0), Integer.parseInt(normalized.substring(1)));
    }

    public int rowIndex() {
        return row - 'A' + 1;
    }

    public String label() {
        return String.valueOf(row) + number;
    }

    @Override
    public int compareTo(Seat other) {
        int byRow = Character.compare(row, other.row);
        return byRow != 0 ? byRow : Integer.compare(number, other.number);
    }

    @Override
    public String toString() {
        return label();
    }
}
