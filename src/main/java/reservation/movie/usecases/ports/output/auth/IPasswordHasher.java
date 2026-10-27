package reservation.movie.usecases.ports.output.auth;

public interface IPasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hash);
}
