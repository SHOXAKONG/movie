package reservation.movie.frameworks.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reservation.movie.adapters.controllers.ReservationController;
import reservation.movie.adapters.presenters.reservation.ReservationViewModel;
import reservation.movie.usecases.ports.input.reservation.ReserveSeatsRequest;

import java.util.List;

@RestController
public class ReservationRestController {

    private final ReservationController controller;

    public ReservationRestController(ReservationController controller) {
        this.controller = controller;
    }

    @PostMapping("/api/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationViewModel reserve(@AuthenticationPrincipal Jwt jwt, @RequestBody ReserveSeatsRequest request) {
        return controller.reserve(CurrentUser.id(jwt), request);
    }

    @GetMapping("/api/reservations")
    public List<ReservationViewModel> mine(@AuthenticationPrincipal Jwt jwt) {
        return controller.listForUser(CurrentUser.id(jwt));
    }

    @PostMapping("/api/reservations/{id}/cancel")
    public ReservationViewModel cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return controller.cancel(CurrentUser.id(jwt), id);
    }
}
