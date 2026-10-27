package reservation.movie.frameworks.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import reservation.movie.frameworks.db.entity.ShowtimeJpaEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeJpaRepository extends JpaRepository<ShowtimeJpaEntity, Long> {

    List<ShowtimeJpaEntity> findByStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTimeAsc(
            LocalDateTime from, LocalDateTime to);

    @Query("""
            select s from ShowtimeJpaEntity s
            where s.auditoriumId = :auditoriumId and s.startTime < :end and s.endTime > :start
            order by s.startTime""")
    List<ShowtimeJpaEntity> findOverlapping(@Param("auditoriumId") Long auditoriumId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);
}
