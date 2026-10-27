package reservation.movie.usecases.ports.output.report;

import java.time.LocalDateTime;

public record ShowtimeOccupancy(Long showtimeId, String movieTitle, String auditoriumName, LocalDateTime startTime,
                                int capacity, int reservedSeats) {
}
