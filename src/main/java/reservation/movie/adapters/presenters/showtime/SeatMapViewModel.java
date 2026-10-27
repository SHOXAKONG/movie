package reservation.movie.adapters.presenters.showtime;

import java.util.List;

/**
 * Seats grouped by row so a client can draw the auditorium directly.
 */
public record SeatMapViewModel(Long showtimeId, int capacity, int availableSeats, List<SeatRow> rows) {

    public record SeatRow(String row, List<SeatView> seats) {
    }

    public record SeatView(String label, int number, boolean available) {
    }
}
