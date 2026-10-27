package reservation.movie.usecases.ports.input.auditorium;

import java.util.List;

public interface IAuditoriumInputPort {

    AuditoriumResponse create(AuditoriumRequest request);

    List<AuditoriumResponse> list();
}
