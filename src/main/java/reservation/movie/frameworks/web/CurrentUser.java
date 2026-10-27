package reservation.movie.frameworks.web;

import org.springframework.security.oauth2.jwt.Jwt;

final class CurrentUser {

    private CurrentUser() {
    }

    static Long id(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
