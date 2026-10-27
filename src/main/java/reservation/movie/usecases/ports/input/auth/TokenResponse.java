package reservation.movie.usecases.ports.input.auth;

import java.time.Instant;

public record TokenResponse(String accessToken, Instant expiresAt, UserResponse user) {
}
