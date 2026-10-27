package reservation.movie.frameworks.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reservation.movie.adapters.controllers.AuditoriumController;
import reservation.movie.adapters.controllers.AuthController;
import reservation.movie.adapters.controllers.ReportController;
import reservation.movie.adapters.controllers.request.AuditoriumHttpRequest;
import reservation.movie.adapters.presenters.auth.UserViewModel;
import reservation.movie.adapters.presenters.reservation.ReservationViewModel;
import reservation.movie.usecases.ports.input.auditorium.AuditoriumResponse;
import reservation.movie.usecases.ports.input.report.OccupancyResponse;
import reservation.movie.usecases.ports.input.report.RevenueReportResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Admin-only endpoints; {@code /api/admin/**} requires ROLE_ADMIN in {@code SecurityConfig}.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final AuthController authController;
    private final AuditoriumController auditoriumController;
    private final ReportController reportController;

    public AdminRestController(AuthController authController, AuditoriumController auditoriumController,
                               ReportController reportController) {
        this.authController = authController;
        this.auditoriumController = auditoriumController;
        this.reportController = reportController;
    }

    @PostMapping("/users/{id}/promote")
    public UserViewModel promote(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return authController.promoteToAdmin(CurrentUser.id(jwt), id);
    }

    @GetMapping("/auditoriums")
    public List<AuditoriumResponse> auditoriums() {
        return auditoriumController.list();
    }

    @PostMapping("/auditoriums")
    @ResponseStatus(HttpStatus.CREATED)
    public AuditoriumResponse createAuditorium(@RequestBody AuditoriumHttpRequest request) {
        return auditoriumController.create(request);
    }

    @GetMapping("/reservations")
    public List<ReservationViewModel> reservations(
            @RequestParam(required = false) Long showtimeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportController.reservations(showtimeId, from, to);
    }

    @GetMapping("/reports/occupancy")
    public List<OccupancyResponse> occupancy(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportController.occupancy(from, to);
    }

    @GetMapping("/reports/revenue")
    public RevenueReportResponse revenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportController.revenue(from, to);
    }
}
