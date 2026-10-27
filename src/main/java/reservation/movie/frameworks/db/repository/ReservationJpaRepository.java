package reservation.movie.frameworks.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import reservation.movie.frameworks.db.entity.ReservationJpaEntity;
import reservation.movie.frameworks.db.entity.ReservationSeatJpaEntity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ReservationJpaRepository extends JpaRepository<ReservationJpaEntity, Long> {

    List<ReservationJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ReservationJpaEntity> findByShowtimeIdOrderByCreatedAtAsc(Long showtimeId);

    @Query("""
            select r from ReservationJpaEntity r
            where r.showtimeId in (
                select s.id from ShowtimeJpaEntity s where s.startTime >= :from and s.startTime < :to)
            order by r.createdAt""")
    List<ReservationJpaEntity> findByShowtimeStartBetween(@Param("from") LocalDateTime from,
                                                          @Param("to") LocalDateTime to);

    boolean existsByShowtimeIdAndStatus(Long showtimeId, String status);

    @Query("""
            select count(r) > 0 from ReservationJpaEntity r
            where r.status = 'CONFIRMED'
              and r.showtimeId in (select s.id from ShowtimeJpaEntity s where s.movieId = :movieId)""")
    boolean existsConfirmedForMovie(@Param("movieId") Long movieId);

    @Query("""
            select s from ReservationSeatJpaEntity s
            where s.showtimeId = :showtimeId and s.active = true""")
    List<ReservationSeatJpaEntity> findActiveSeats(@Param("showtimeId") Long showtimeId);

    @Query("""
            select s.showtimeId, count(s) from ReservationSeatJpaEntity s
            where s.active = true and s.showtimeId in :showtimeIds
            group by s.showtimeId""")
    List<Object[]> countActiveSeats(@Param("showtimeIds") Collection<Long> showtimeIds);

    /** Seat rows go with them through the {@code reservation_seats.reservation_id} ON DELETE CASCADE. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ReservationJpaEntity r where r.status = 'CANCELLED' and r.showtimeId = :showtimeId")
    void deleteCancelledForShowtime(@Param("showtimeId") Long showtimeId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            delete from ReservationJpaEntity r
            where r.status = 'CANCELLED'
              and r.showtimeId in (select s.id from ShowtimeJpaEntity s where s.movieId = :movieId)""")
    void deleteCancelledForMovie(@Param("movieId") Long movieId);
}
