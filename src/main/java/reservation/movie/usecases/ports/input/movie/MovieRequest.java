package reservation.movie.usecases.ports.input.movie;

import java.util.List;

public record MovieRequest(String title, String description, String posterUrl, Integer durationMinutes,
                           List<String> genres) {
}
