package reservation.movie.adapters.controllers;

import reservation.movie.adapters.controllers.request.ReserveSeatsHttpRequest;
import reservation.movie.adapters.presenters.reservation.ReservationPresenter;
import reservation.movie.adapters.presenters.reservation.ReservationViewModel;
import reservation.movie.usecases.ports.input.reservation.IReservationInputPort;
import reservation.movie.usecases.ports.input.reservation.ReserveSeatsRequest;

import java.util.List;

public class ReservationController {

    private final IReservationInputPort reservations;
    private final ReservationPresenter presenter;

    public ReservationController(IReservationInputPort reservations, ReservationPresenter presenter) {
        this.reservations = reservations;
        this.presenter = presenter;
    }

    public ReservationViewModel reserve(Long userId, ReserveSeatsHttpRequest body) {
        return presenter.present(reservations.reserve(userId, new ReserveSeatsRequest(body.showtimeId(), body.seats())));
    }

    public List<ReservationViewModel> listForUser(Long userId) {
        return presenter.present(reservations.listForUser(userId));
    }

    public ReservationViewModel cancel(Long userId, Long reservationId) {
        return presenter.present(reservations.cancel(userId, reservationId));
    }
}
