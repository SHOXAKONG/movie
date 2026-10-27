package reservation.movie.usecases.ports.input.auth;

public interface IAuthInputPort {

    UserResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    UserResponse getUser(Long userId);

    /** Only an admin may promote another user. */
    UserResponse promoteToAdmin(Long actorId, Long userId);

    /** Seeds the initial admin account if no user with that email exists yet. */
    void ensureAdminExists(String email, String password, String fullName);
}
