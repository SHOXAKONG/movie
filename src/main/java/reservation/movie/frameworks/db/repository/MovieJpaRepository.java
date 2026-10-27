package reservation.movie.frameworks.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import reservation.movie.frameworks.db.entity.MovieJpaEntity;

import java.util.List;

public interface MovieJpaRepository extends JpaRepository<MovieJpaEntity, Long> {

    List<MovieJpaEntity> findAllByOrderByTitleAsc();

    @Query("""
            select distinct m from MovieJpaEntity m join m.genres g
            where lower(g) = lower(:genre)
            order by m.title""")
    List<MovieJpaEntity> findByGenre(@Param("genre") String genre);
}
