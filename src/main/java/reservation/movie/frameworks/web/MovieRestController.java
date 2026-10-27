package reservation.movie.frameworks.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reservation.movie.adapters.controllers.MovieController;
import reservation.movie.adapters.controllers.request.MovieHttpRequest;
import reservation.movie.adapters.presenters.movie.MovieViewModel;

import java.util.List;

@RestController
public class MovieRestController {

    private final MovieController controller;

    public MovieRestController(MovieController controller) {
        this.controller = controller;
    }

    @GetMapping("/api/movies")
    public List<MovieViewModel> list(@RequestParam(required = false) String genre) {
        return controller.list(genre);
    }

    @GetMapping("/api/movies/{id}")
    public MovieViewModel get(@PathVariable Long id) {
        return controller.get(id);
    }

    @PostMapping("/api/admin/movies")
    @ResponseStatus(HttpStatus.CREATED)
    public MovieViewModel create(@RequestBody MovieHttpRequest request) {
        return controller.create(request);
    }

    @PutMapping("/api/admin/movies/{id}")
    public MovieViewModel update(@PathVariable Long id, @RequestBody MovieHttpRequest request) {
        return controller.update(id, request);
    }

    @DeleteMapping("/api/admin/movies/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        controller.delete(id);
    }
}
