package reservation.movie.main;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reservation.movie.adapters.controllers.AuditoriumController;
import reservation.movie.adapters.controllers.AuthController;
import reservation.movie.adapters.controllers.MovieController;
import reservation.movie.adapters.controllers.ReportController;
import reservation.movie.adapters.controllers.ReservationController;
import reservation.movie.adapters.controllers.ShowtimeController;
import reservation.movie.adapters.gateways.auditorium.AuditoriumDataSource;
import reservation.movie.adapters.gateways.auditorium.AuditoriumGateway;
import reservation.movie.adapters.gateways.movie.MovieDataSource;
import reservation.movie.adapters.gateways.movie.MovieGateway;
import reservation.movie.adapters.gateways.report.ReportDataSource;
import reservation.movie.adapters.gateways.report.ReportGateway;
import reservation.movie.adapters.gateways.reservation.ReservationDataSource;
import reservation.movie.adapters.gateways.reservation.ReservationGateway;
import reservation.movie.adapters.gateways.showtime.ShowtimeDataSource;
import reservation.movie.adapters.gateways.showtime.ShowtimeGateway;
import reservation.movie.adapters.gateways.user.UserDataSource;
import reservation.movie.adapters.gateways.user.UserGateway;
import reservation.movie.adapters.presenters.auth.AuthPresenter;
import reservation.movie.adapters.presenters.movie.MoviePresenter;
import reservation.movie.adapters.presenters.reservation.ReservationPresenter;
import reservation.movie.adapters.presenters.showtime.ShowtimePresenter;
import reservation.movie.usecases.interactors.AuditoriumInteractor;
import reservation.movie.usecases.interactors.AuthInteractor;
import reservation.movie.usecases.interactors.MovieInteractor;
import reservation.movie.usecases.interactors.ReportInteractor;
import reservation.movie.usecases.interactors.ReservationInteractor;
import reservation.movie.usecases.interactors.ShowtimeInteractor;
import reservation.movie.usecases.ports.input.auth.IAuthInputPort;
import reservation.movie.usecases.ports.output.auditorium.IAuditoriumRepository;
import reservation.movie.usecases.ports.output.auth.IPasswordHasher;
import reservation.movie.usecases.ports.output.auth.ITokenIssuer;
import reservation.movie.usecases.ports.output.auth.IUserRepository;
import reservation.movie.usecases.ports.output.movie.IMovieRepository;
import reservation.movie.usecases.ports.output.report.IReportRepository;
import reservation.movie.usecases.ports.output.reservation.IReservationRepository;
import reservation.movie.usecases.ports.output.showtime.IShowtimeRepository;

import java.time.Clock;

/**
 * Composition root: data sources (frameworks.db) -> gateways -> interactors -> presenters -> controllers.
 * Every object in the inner layers is created here and nowhere else.
 */
@Configuration
public class MovieConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    // ---- gateways ----

    @Bean
    public IUserRepository userRepository(UserDataSource dataSource) {
        return new UserGateway(dataSource);
    }

    @Bean
    public IMovieRepository movieRepository(MovieDataSource dataSource) {
        return new MovieGateway(dataSource);
    }

    @Bean
    public IAuditoriumRepository auditoriumRepository(AuditoriumDataSource dataSource) {
        return new AuditoriumGateway(dataSource);
    }

    @Bean
    public IShowtimeRepository showtimeRepository(ShowtimeDataSource dataSource) {
        return new ShowtimeGateway(dataSource);
    }

    @Bean
    public IReservationRepository reservationRepository(ReservationDataSource dataSource) {
        return new ReservationGateway(dataSource);
    }

    @Bean
    public IReportRepository reportRepository(ReportDataSource dataSource) {
        return new ReportGateway(dataSource);
    }

    // ---- use cases ----

    @Bean
    public IAuthInputPort authInputPort(IUserRepository users, IPasswordHasher hasher, ITokenIssuer tokens) {
        return new AuthInteractor(users, hasher, tokens);
    }

    // ---- presenters & controllers ----

    @Bean
    public MoviePresenter moviePresenter() {
        return new MoviePresenter();
    }

    @Bean
    public ReservationPresenter reservationPresenter() {
        return new ReservationPresenter();
    }

    @Bean
    public AuthController authController(IAuthInputPort authInputPort) {
        return new AuthController(authInputPort, new AuthPresenter());
    }

    @Bean
    public MovieController movieController(IMovieRepository movies, IReservationRepository reservations,
                                           MoviePresenter moviePresenter) {
        return new MovieController(new MovieInteractor(movies, reservations), moviePresenter);
    }

    @Bean
    public AuditoriumController auditoriumController(IAuditoriumRepository auditoriums) {
        return new AuditoriumController(new AuditoriumInteractor(auditoriums));
    }

    @Bean
    public ShowtimeController showtimeController(IShowtimeRepository showtimes, IMovieRepository movies,
                                                 IAuditoriumRepository auditoriums,
                                                 IReservationRepository reservations, MoviePresenter moviePresenter,
                                                 Clock clock) {
        return new ShowtimeController(
                new ShowtimeInteractor(showtimes, movies, auditoriums, reservations, clock),
                new ShowtimePresenter(moviePresenter));
    }

    @Bean
    public ReservationController reservationController(IReservationRepository reservations,
                                                       IShowtimeRepository showtimes, IMovieRepository movies,
                                                       IAuditoriumRepository auditoriums,
                                                       ReservationPresenter reservationPresenter, Clock clock) {
        return new ReservationController(
                new ReservationInteractor(reservations, showtimes, movies, auditoriums, clock), reservationPresenter);
    }

    @Bean
    public ReportController reportController(IReservationRepository reservations, IReportRepository reports,
                                             IShowtimeRepository showtimes, IMovieRepository movies,
                                             IAuditoriumRepository auditoriums,
                                             ReservationPresenter reservationPresenter, Clock clock) {
        return new ReportController(
                new ReportInteractor(reservations, reports, showtimes, movies, auditoriums, clock),
                reservationPresenter);
    }
}
