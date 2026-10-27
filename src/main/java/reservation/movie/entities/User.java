package reservation.movie.entities;

import java.util.Locale;
import java.util.regex.Pattern;

public class User {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String fullName;
    private final Role role;

    public User(Long id, String email, String passwordHash, String fullName, Role role) {
        if (email == null || !EMAIL.matcher(email.strip()).matches()) {
            throw new DomainException("A valid email is required");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new DomainException("Password hash is required");
        }
        this.id = id;
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        this.fullName = fullName == null ? null : fullName.strip();
        this.role = role == null ? Role.USER : role;
    }

    public static User register(String email, String passwordHash, String fullName) {
        return new User(null, email, passwordHash, fullName, Role.USER);
    }

    public static User admin(String email, String passwordHash, String fullName) {
        return new User(null, email, passwordHash, fullName, Role.ADMIN);
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }

    public User promoteToAdmin() {
        if (isAdmin()) {
            throw new DomainException("User is already an admin");
        }
        return new User(id, email, passwordHash, fullName, Role.ADMIN);
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }
}
