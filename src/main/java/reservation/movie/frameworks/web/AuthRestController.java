package reservation.movie.frameworks.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reservation.movie.adapters.controllers.AuthController;
import reservation.movie.adapters.controllers.request.LoginHttpRequest;
import reservation.movie.adapters.controllers.request.RegisterHttpRequest;
import reservation.movie.adapters.presenters.auth.TokenViewModel;
import reservation.movie.adapters.presenters.auth.UserViewModel;

@RestController
public class AuthRestController {

    private final AuthController controller;

    public AuthRestController(AuthController controller) {
        this.controller = controller;
    }

    @PostMapping("/api/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserViewModel register(@RequestBody RegisterHttpRequest request) {
        return controller.register(request);
    }

    @PostMapping("/api/auth/login")
    public TokenViewModel login(@RequestBody LoginHttpRequest request) {
        return controller.login(request);
    }

    @GetMapping("/api/users/me")
    public UserViewModel me(@AuthenticationPrincipal Jwt jwt) {
        return controller.currentUser(CurrentUser.id(jwt));
    }
}
