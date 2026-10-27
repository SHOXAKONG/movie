package reservation.movie.usecases.interactors;

import reservation.movie.entities.Auditorium;
import reservation.movie.usecases.exceptions.ConflictException;
import reservation.movie.usecases.ports.input.auditorium.AuditoriumRequest;
import reservation.movie.usecases.ports.input.auditorium.AuditoriumResponse;
import reservation.movie.usecases.ports.input.auditorium.IAuditoriumInputPort;
import reservation.movie.usecases.ports.output.auditorium.IAuditoriumRepository;

import java.util.List;

public class AuditoriumInteractor implements IAuditoriumInputPort {

    private final IAuditoriumRepository auditoriums;

    public AuditoriumInteractor(IAuditoriumRepository auditoriums) {
        this.auditoriums = auditoriums;
    }

    @Override
    public AuditoriumResponse create(AuditoriumRequest request) {
        Auditorium auditorium = Auditorium.create(request.name(), Responses.orZero(request.rows()),
                Responses.orZero(request.seatsPerRow()));
        if (auditoriums.existsByName(auditorium.getName())) {
            throw new ConflictException("An auditorium named '" + auditorium.getName() + "' already exists");
        }
        return Responses.auditorium(auditoriums.save(auditorium));
    }

    @Override
    public List<AuditoriumResponse> list() {
        return auditoriums.findAll().stream().map(Responses::auditorium).toList();
    }
}
