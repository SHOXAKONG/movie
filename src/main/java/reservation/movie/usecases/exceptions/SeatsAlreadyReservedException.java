package reservation.movie.usecases.exceptions;

import java.util.Collection;

public class SeatsAlreadyReservedException extends ConflictException {

    public SeatsAlreadyReservedException(Collection<String> seats) {
        super("Seats already reserved: " + String.join(", ", seats));
    }

    public SeatsAlreadyReservedException() {
        super("One or more of the selected seats were just reserved by someone else");
    }
}
