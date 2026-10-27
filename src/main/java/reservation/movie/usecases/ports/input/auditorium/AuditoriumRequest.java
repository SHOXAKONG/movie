package reservation.movie.usecases.ports.input.auditorium;

public record AuditoriumRequest(String name, Integer rows, Integer seatsPerRow) {
}
