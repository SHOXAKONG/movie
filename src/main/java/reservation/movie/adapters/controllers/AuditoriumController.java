package reservation.movie.adapters.controllers;

import reservation.movie.usecases.ports.input.auditorium.AuditoriumRequest;
import reservation.movie.usecases.ports.input.auditorium.AuditoriumResponse;
import reservation.movie.usecases.ports.input.auditorium.IAuditoriumInputPort;

import java.util.List;

/**
 * Auditorium responses are already display-ready, so no presenter is involved.
 */
public class AuditoriumController {

    private final IAuditoriumInputPort auditoriums;

    public AuditoriumController(IAuditoriumInputPort auditoriums) {
        this.auditoriums = auditoriums;
    }

    public AuditoriumResponse create(AuditoriumRequest request) {
        return auditoriums.create(request);
    }

    public List<AuditoriumResponse> list() {
        return auditoriums.list();
    }
}
