package reservation.movie.usecases.ports.input.movie;

import java.util.List;

public record MovieResponse(Long id, String title, String description, String posterUrl, int durationMinutes,
                            List<String> genres) {
}
