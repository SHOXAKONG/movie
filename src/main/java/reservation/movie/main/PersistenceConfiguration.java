package reservation.movie.main;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "reservation.movie.frameworks.db")
@EnableJpaRepositories(basePackages = "reservation.movie.frameworks.db")
public class PersistenceConfiguration {
}
