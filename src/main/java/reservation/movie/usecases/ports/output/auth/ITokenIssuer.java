package reservation.movie.usecases.ports.output.auth;

import reservation.movie.entities.User;

import java.time.Instant;

public interface ITokenIssuer {

    IssuedToken issue(User user);

    record IssuedToken(String value, Instant expiresAt) {
    }
}
