package reservation.movie.frameworks.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reservation.movie.adapters.controllers.ShowtimeController;
import reservation.movie.adapters.controllers.request.ScheduleShowtimeHttpRequest;
import reservation.movie.adapters.presenters.showtime.MovieScheduleViewModel;
import reservation.movie.adapters.presenters.showtime.SeatMapViewModel;
import reservation.movie.adapters.presenters.showtime.ShowtimeViewModel;

import java.time.LocalDate;
import java.util.List;

@RestController
public class ShowtimeRestController {

    private final ShowtimeController controller;

    public ShowtimeRestController(ShowtimeController controller) {
        this.controller = controller;
    }

    /** Movies and their bookable showtimes on a day (today when {@code date} is omitted). */
    @GetMapping("/api/showtimes")
    public List<MovieScheduleViewModel> forDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return controller.listForDate(date);
    }

    @GetMapping("/api/showtimes/{id}")
    public ShowtimeViewModel get(@PathVariable Long id) {
        return controller.get(id);
    }

    @GetMapping("/api/showtimes/{id}/seats")
    public SeatMapViewModel seats(@PathVariable Long id) {
        return controller.seatMap(id);
    }

    @PostMapping("/api/admin/showtimes")
    @ResponseStatus(HttpStatus.CREATED)
    public ShowtimeViewModel schedule(@RequestBody ScheduleShowtimeHttpRequest request) {
        return controller.schedule(request);
    }

    @DeleteMapping("/api/admin/showtimes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        controller.delete(id);
    }
}
