package reservation.movie.adapters.controllers;

import reservation.movie.adapters.controllers.request.LoginHttpRequest;
import reservation.movie.adapters.controllers.request.RegisterHttpRequest;
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

    public UserViewModel register(RegisterHttpRequest body) {
        return presenter.present(auth.register(new RegisterRequest(body.email(), body.password(), body.fullName())));
    }

    public TokenViewModel login(LoginHttpRequest body) {
        return presenter.present(auth.login(new LoginRequest(body.email(), body.password())));
    }

    public UserViewModel currentUser(Long userId) {
        return presenter.present(auth.getUser(userId));
    }

    public UserViewModel promoteToAdmin(Long actorId, Long userId) {
        return presenter.present(auth.promoteToAdmin(actorId, userId));
    }
}
