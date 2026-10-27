package reservation.movie.usecases.ports.input.auditorium;

public record AuditoriumResponse(Long id, String name, int rows, int seatsPerRow, int capacity) {
}
