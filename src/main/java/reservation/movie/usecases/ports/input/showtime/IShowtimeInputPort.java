package reservation.movie.usecases.ports.input.showtime;

import java.time.LocalDate;
import java.util.List;

public interface IShowtimeInputPort {

    ShowtimeResponse schedule(ScheduleShowtimeRequest request);

    /** Refused while the showtime still has confirmed reservations. */
    void delete(Long showtimeId);

    ShowtimeResponse get(Long showtimeId);

    /** Movies that have showtimes on {@code date}, each with its showtimes for that day. */
    List<MovieScheduleResponse> listForDate(LocalDate date);

    SeatMapResponse seatMap(Long showtimeId);
}
