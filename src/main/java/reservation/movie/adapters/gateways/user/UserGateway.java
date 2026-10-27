package reservation.movie.adapters.gateways.user;

import reservation.movie.entities.Role;
import reservation.movie.entities.User;
import reservation.movie.usecases.ports.output.auth.IUserRepository;

import java.util.Optional;

public class UserGateway implements IUserRepository {

    private final UserDataSource dataSource;

    public UserGateway(UserDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User save(User user) {
        return toEntity(dataSource.save(new UserRecord(user.getId(), user.getEmail(), user.getPasswordHash(),
                user.getFullName(), user.getRole().name())));
    }

    @Override
    public Optional<User> findById(Long id) {
        return dataSource.findById(id).map(UserGateway::toEntity);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return dataSource.findByEmail(email).map(UserGateway::toEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return dataSource.existsByEmail(email);
    }

    private static User toEntity(UserRecord record) {
        return new User(record.id(), record.email(), record.passwordHash(), record.fullName(),
                Role.valueOf(record.role()));
    }
}
