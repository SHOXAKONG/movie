package reservation.movie.adapters.gateways.user;

public record UserRecord(Long id, String email, String passwordHash, String fullName, String role) {
}
