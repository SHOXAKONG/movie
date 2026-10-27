package reservation.movie.usecases.ports.output.auth;

import reservation.movie.entities.User;

import java.util.Optional;

public interface IUserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
