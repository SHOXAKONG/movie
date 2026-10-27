package reservation.movie.usecases.ports.input.auth;

public record RegisterRequest(String email, String password, String fullName) {
}
