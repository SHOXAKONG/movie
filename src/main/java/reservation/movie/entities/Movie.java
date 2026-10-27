package reservation.movie.entities;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class Movie {

    private static final int MAX_DURATION_MINUTES = 600;
    private static final int MAX_TITLE_LENGTH = 255;

    private final Long id;
    private final String title;
    private final String description;
    private final String posterUrl;
    private final int durationMinutes;
    private final List<String> genres;

    public Movie(Long id, String title, String description, String posterUrl, int durationMinutes,
                 Collection<String> genres) {
        if (title == null || title.isBlank()) {
            throw new DomainException("Movie title must not be blank");
        }
        if (title.strip().length() > MAX_TITLE_LENGTH) {
            throw new DomainException("Movie title must be at most " + MAX_TITLE_LENGTH + " characters");
        }
        if (durationMinutes <= 0 || durationMinutes > MAX_DURATION_MINUTES) {
            throw new DomainException("Movie duration must be between 1 and " + MAX_DURATION_MINUTES + " minutes");
        }
        if (posterUrl != null && !posterUrl.isBlank() && !posterUrl.matches("^https?://\\S+$")) {
            throw new DomainException("Poster URL must be an http(s) URL");
        }
        this.id = id;
        this.title = title.strip();
        this.description = description;
        this.posterUrl = posterUrl == null || posterUrl.isBlank() ? null : posterUrl.strip();
        this.durationMinutes = durationMinutes;
        this.genres = normalizeGenres(genres);
        if (this.genres.isEmpty()) {
            throw new DomainException("A movie needs at least one genre");
        }
    }

    public static Movie create(String title, String description, String posterUrl, int durationMinutes,
                               Collection<String> genres) {
        return new Movie(null, title, description, posterUrl, durationMinutes, genres);
    }

    public Movie update(String title, String description, String posterUrl, int durationMinutes,
                        Collection<String> genres) {
        return new Movie(id, title, description, posterUrl, durationMinutes, genres);
    }

    public Movie withId(Long id) {
        return new Movie(id, title, description, posterUrl, durationMinutes, genres);
    }

    public boolean hasGenre(String genre) {
        return genres.stream().anyMatch(g -> g.equalsIgnoreCase(genre.strip()));
    }

    /**
     * Trims, drops blanks and removes case-insensitive duplicates, keeping the first spelling.
     */
    private static List<String> normalizeGenres(Collection<String> genres) {
        if (genres == null) {
            return List.of();
        }
        Map<String, String> unique = new LinkedHashMap<>();
        for (String genre : genres) {
            if (genre != null && !genre.isBlank()) {
                unique.putIfAbsent(genre.strip().toLowerCase(Locale.ROOT), genre.strip());
            }
        }
        return List.copyOf(unique.values());
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public List<String> getGenres() {
        return genres;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Movie movie)) return false;
        return id != null && Objects.equals(id, movie.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
