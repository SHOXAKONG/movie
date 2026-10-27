package reservation.movie.adapters.presenters.reservation;

import reservation.movie.usecases.ports.input.reservation.ReservationResponse;

import java.util.Comparator;
import java.util.List;

public class ReservationPresenter {

    public ReservationViewModel present(ReservationResponse r) {
        return new ReservationViewModel(r.id(), r.userId(), r.showtimeId(), r.movieTitle(), r.auditoriumName(),
                r.startTime(), r.seats(), r.seats().size(), r.totalPrice(), r.status(), r.createdAt(),
                r.cancelledAt(), r.cancellable());
    }

    /** Most recent showtime first. */
    public List<ReservationViewModel> present(List<ReservationResponse> reservations) {
        return reservations.stream()
                .sorted(Comparator.comparing(ReservationResponse::startTime).reversed()
                        .thenComparing(ReservationResponse::id))
                .map(this::present)
                .toList();
    }
}
