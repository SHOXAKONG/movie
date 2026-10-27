package reservation.movie.frameworks.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reservation.movie.usecases.ports.input.auth.IAuthInputPort;

/**
 * Creates the initial admin from {@code app.admin.*} on startup. Only admins can promote others,
 * so without this seed nobody could ever become one.
 */
@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final IAuthInputPort auth;
    private final String email;
    private final String password;
    private final String fullName;

    public AdminSeeder(IAuthInputPort auth,
                       @Value("${app.admin.email:}") String email,
                       @Value("${app.admin.password:}") String password,
                       @Value("${app.admin.full-name:Administrator}") String fullName) {
        this.auth = auth;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank()) {
            log.warn("app.admin.email is not set; no initial admin will be created");
            return;
        }
        auth.ensureAdminExists(email, password, fullName);
        log.info("Initial admin account ensured for {}", email);
    }
}
