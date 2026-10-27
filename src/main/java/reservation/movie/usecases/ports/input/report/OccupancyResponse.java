package reservation.movie.usecases.ports.input.report;

import java.time.LocalDateTime;

public record OccupancyResponse(Long showtimeId, String movieTitle, String auditoriumName, LocalDateTime startTime,
                                int capacity, int reservedSeats, int availableSeats, double occupancyPercent) {
}
