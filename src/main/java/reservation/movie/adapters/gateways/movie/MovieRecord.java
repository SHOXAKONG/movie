package reservation.movie.adapters.gateways.movie;

import java.util.List;

/**
 * Persistence-shaped data structure exchanged with the database driver in {@code frameworks.db}.
 */
public record MovieRecord(Long id, String title, String description, String posterUrl, int durationMinutes,
                          List<String> genres) {
}
