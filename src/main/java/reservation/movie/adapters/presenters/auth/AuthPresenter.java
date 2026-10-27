package reservation.movie.adapters.presenters.auth;

import reservation.movie.usecases.ports.input.auth.TokenResponse;
import reservation.movie.usecases.ports.input.auth.UserResponse;

public class AuthPresenter {

    public UserViewModel present(UserResponse user) {
        return new UserViewModel(user.id(), user.email(), user.fullName(), user.role(), "ADMIN".equals(user.role()));
    }

    public TokenViewModel present(TokenResponse token) {
        return new TokenViewModel(token.accessToken(), "Bearer", token.expiresAt(), present(token.user()));
    }
}
