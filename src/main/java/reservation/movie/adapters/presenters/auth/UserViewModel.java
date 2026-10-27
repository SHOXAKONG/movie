package reservation.movie.adapters.presenters.auth;

public record UserViewModel(Long id, String email, String fullName, String role, boolean admin) {
}
