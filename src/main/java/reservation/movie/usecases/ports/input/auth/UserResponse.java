package reservation.movie.usecases.ports.input.auth;

public record UserResponse(Long id, String email, String fullName, String role) {
}
