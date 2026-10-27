package reservation.movie.adapters.controllers.request;

import java.util.List;

public record MovieHttpRequest(String title, String description, String posterUrl, Integer durationMinutes,
                               List<String> genres) {
}
