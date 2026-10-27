package reservation.movie.adapters.presenters.movie;

import java.util.List;

public record MovieViewModel(Long id, String title, String description, String posterUrl, int durationMinutes,
                             String duration, List<String> genres) {
}
