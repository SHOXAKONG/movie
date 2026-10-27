package reservation.movie.adapters.gateways.user;

import java.util.Optional;

public interface UserDataSource {

    /** Must throw {@code ConflictException} when the email is already taken. */
    UserRecord save(UserRecord user);

    Optional<UserRecord> findById(Long id);

    Optional<UserRecord> findByEmail(String email);

    boolean existsByEmail(String email);
}
