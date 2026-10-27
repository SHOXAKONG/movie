package reservation.movie.usecases.ports.input.reservation;

import java.util.List;

public interface IReservationInputPort {

    ReservationResponse reserve(Long userId, ReserveSeatsRequest request);

    List<ReservationResponse> listForUser(Long userId);

    /** Users may cancel only their own, upcoming reservations. */
    ReservationResponse cancel(Long userId, Long reservationId);
}
