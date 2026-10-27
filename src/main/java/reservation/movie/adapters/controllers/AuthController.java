package reservation.movie.adapters.controllers;

import reservation.movie.adapters.presenters.auth.AuthPresenter;
import reservation.movie.adapters.presenters.auth.TokenViewModel;
import reservation.movie.adapters.presenters.auth.UserViewModel;
import reservation.movie.usecases.ports.input.auth.IAuthInputPort;
import reservation.movie.usecases.ports.input.auth.LoginRequest;
import reservation.movie.usecases.ports.input.auth.RegisterRequest;

public class AuthController {

    private final IAuthInputPort auth;
    private final AuthPresenter presenter;

    public AuthController(IAuthInputPort auth, AuthPresenter presenter) {
        this.auth = auth;
        this.presenter = presenter;
    }

    public UserViewModel register(RegisterRequest request) {
        return presenter.present(auth.register(request));
    }

    public TokenViewModel login(LoginRequest request) {
        return presenter.present(auth.login(request));
    }

    public UserViewModel currentUser(Long userId) {
        return presenter.present(auth.getUser(userId));
    }

    public UserViewModel promoteToAdmin(Long actorId, Long userId) {
        return presenter.present(auth.promoteToAdmin(actorId, userId));
    }
}
