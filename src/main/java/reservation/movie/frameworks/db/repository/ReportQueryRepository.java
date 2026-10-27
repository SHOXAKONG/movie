package reservation.movie.frameworks.db.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import reservation.movie.frameworks.db.entity.ShowtimeJpaEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Native SQL aggregates for the admin reports.
 */
public interface ReportQueryRepository extends Repository<ShowtimeJpaEntity, Long> {

    interface OccupancyRow {
        Long getShowtimeId();

        String getMovieTitle();

        String getAuditoriumName();

        LocalDateTime getStartTime();

        Integer getCapacity();

        Long getReservedSeats();
    }

    interface RevenueRow {
        Long getMovieId();

        String getMovieTitle();

        Long getReservations();

        Long getSeatsSold();

        BigDecimal getRevenue();
    }

    @Query(nativeQuery = true, value = """
            select s.id                                as "showtimeId",
                   m.title                             as "movieTitle",
                   a.name                              as "auditoriumName",
                   s.start_time                        as "startTime",
                   a.row_count * a.seats_per_row       as "capacity",
                   (select count(*) from reservation_seats rs
                     where rs.showtime_id = s.id and rs.active) as "reservedSeats"
            from showtimes s
            join movies m on m.id = s.movie_id
            join auditoriums a on a.id = s.auditorium_id
            where s.start_time >= :from and s.start_time < :to
            order by s.start_time, a.name""")
    List<OccupancyRow> occupancy(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(nativeQuery = true, value = """
            with seat_counts as (
                select reservation_id, count(*) as seats from reservation_seats group by reservation_id
            )
            select m.id                              as "movieId",
                   m.title                           as "movieTitle",
                   count(r.id)                       as "reservations",
                   coalesce(sum(sc.seats), 0)        as "seatsSold",
                   coalesce(sum(r.total_price), 0)   as "revenue"
            from reservations r
            join seat_counts sc on sc.reservation_id = r.id
            join showtimes s on s.id = r.showtime_id
            join movies m on m.id = s.movie_id
            where r.status = 'CONFIRMED' and s.start_time >= :from and s.start_time < :to
            group by m.id, m.title
            order by coalesce(sum(r.total_price), 0) desc, m.title""")
    List<RevenueRow> revenueByMovie(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
