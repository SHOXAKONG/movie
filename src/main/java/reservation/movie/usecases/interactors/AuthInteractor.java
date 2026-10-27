package reservation.movie.usecases.interactors;

import reservation.movie.entities.DomainException;
import reservation.movie.entities.User;
import reservation.movie.usecases.exceptions.AuthenticationFailedException;
import reservation.movie.usecases.exceptions.ConflictException;
import reservation.movie.usecases.exceptions.ForbiddenException;
import reservation.movie.usecases.exceptions.NotFoundException;
import reservation.movie.usecases.ports.input.auth.IAuthInputPort;
import reservation.movie.usecases.ports.input.auth.LoginRequest;
import reservation.movie.usecases.ports.input.auth.RegisterRequest;
import reservation.movie.usecases.ports.input.auth.TokenResponse;
import reservation.movie.usecases.ports.input.auth.UserResponse;
import reservation.movie.usecases.ports.output.auth.IPasswordHasher;
import reservation.movie.usecases.ports.output.auth.ITokenIssuer;
import reservation.movie.usecases.ports.output.auth.IUserRepository;

public class AuthInteractor implements IAuthInputPort {

    static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 72;

    private final IUserRepository users;
    private final IPasswordHasher passwordHasher;
    private final ITokenIssuer tokenIssuer;

    public AuthInteractor(IUserRepository users, IPasswordHasher passwordHasher, ITokenIssuer tokenIssuer) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        validatePassword(request.password());
        String email = User.normalizeEmail(request.email());
        if (email != null && users.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = User.register(email, passwordHasher.hash(request.password()), request.fullName());
        return Responses.user(users.save(user));
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        String email = User.normalizeEmail(request.email());
        if (email == null || request.password() == null) {
            throw new AuthenticationFailedException();
        }
        User user = users.findByEmail(email).orElseThrow(AuthenticationFailedException::new);
        if (!passwordHasher.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationFailedException();
        }
        ITokenIssuer.IssuedToken token = tokenIssuer.issue(user);
        return new TokenResponse(token.value(), token.expiresAt(), Responses.user(user));
    }

    @Override
    public UserResponse getUser(Long userId) {
        return users.findById(userId).map(Responses::user).orElseThrow(() -> NotFoundException.of("User", userId));
    }

    @Override
    public UserResponse promoteToAdmin(Long actorId, Long userId) {
        User actor = users.findById(actorId).orElseThrow(() -> new ForbiddenException("Only admins can promote users"));
        if (!actor.isAdmin()) {
            throw new ForbiddenException("Only admins can promote users");
        }
        User user = users.findById(userId).orElseThrow(() -> NotFoundException.of("User", userId));
        return Responses.user(users.save(user.promoteToAdmin()));
    }

    @Override
    public void ensureAdminExists(String email, String password, String fullName) {
        String normalized = User.normalizeEmail(email);
        if (normalized == null || normalized.isBlank() || users.existsByEmail(normalized)) {
            return;
        }
        validatePassword(password);
        users.save(User.admin(normalized, passwordHasher.hash(password), fullName));
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new DomainException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new DomainException("Password must be at most " + MAX_PASSWORD_LENGTH + " characters");
        }
    }
}
