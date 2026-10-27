package reservation.movie.usecases.ports.input.showtime;

import java.util.List;

public record SeatMapResponse(Long showtimeId, int rows, int seatsPerRow, int availableSeats, List<SeatStatus> seats) {

    public record SeatStatus(String label, String row, int number, boolean available) {
    }
}
