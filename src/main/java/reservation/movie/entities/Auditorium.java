package reservation.movie.entities;

import java.util.ArrayList;
import java.util.List;

public class Auditorium {

    public static final int MAX_ROWS = 26;
    public static final int MAX_SEATS_PER_ROW = 50;

    private final Long id;
    private final String name;
    private final int rows;
    private final int seatsPerRow;

    public Auditorium(Long id, String name, int rows, int seatsPerRow) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Auditorium name must not be blank");
        }
        if (rows < 1 || rows > MAX_ROWS) {
            throw new DomainException("Auditorium must have between 1 and " + MAX_ROWS + " rows");
        }
        if (seatsPerRow < 1 || seatsPerRow > MAX_SEATS_PER_ROW) {
            throw new DomainException("Auditorium must have between 1 and " + MAX_SEATS_PER_ROW + " seats per row");
        }
        this.id = id;
        this.name = name.strip();
        this.rows = rows;
        this.seatsPerRow = seatsPerRow;
    }

    public static Auditorium create(String name, int rows, int seatsPerRow) {
        return new Auditorium(null, name, rows, seatsPerRow);
    }

    public int capacity() {
        return rows * seatsPerRow;
    }

    public boolean contains(Seat seat) {
        return seat.rowIndex() <= rows && seat.number() <= seatsPerRow;
    }

    public List<Seat> allSeats() {
        List<Seat> seats = new ArrayList<>(capacity());
        for (int r = 0; r < rows; r++) {
            for (int n = 1; n <= seatsPerRow; n++) {
                seats.add(new Seat((char) ('A' + r), n));
            }
        }
        return seats;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getRows() {
        return rows;
    }

    public int getSeatsPerRow() {
        return seatsPerRow;
    }
}
