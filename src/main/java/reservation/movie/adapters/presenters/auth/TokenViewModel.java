package reservation.movie.adapters.presenters.auth;

import java.time.Instant;

public record TokenViewModel(String accessToken, String tokenType, Instant expiresAt, UserViewModel user) {
}
