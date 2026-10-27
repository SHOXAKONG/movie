package reservation.movie.unit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import reservation.movie.frameworks.security.SecurityConfig;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Guards against shipping usable credentials: outside the dev profile the JWT secret and the
 * admin account must come from the environment.
 */
class ConfigurationDefaultsTest {

    private static Properties load(String file) {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource(file));
        return yaml.getObject();
    }

    @Test
    void defaultConfigurationHasNoSecretFallbacks() {
        Properties props = load("application.yaml");

        assertThat(props.getProperty("app.jwt.secret")).isEqualTo("${JWT_SECRET:}");
        assertThat(props.getProperty("app.admin.email")).isEqualTo("${ADMIN_EMAIL:}");
        assertThat(props.getProperty("app.admin.password")).isEqualTo("${ADMIN_PASSWORD:}");
    }

    @Test
    void missingSecretFailsWithAHelpfulMessage() {
        assertThatThrownBy(() -> new SecurityConfig().jwtSigningKey(""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET is not set")
                .hasMessageContaining("dev");
        assertThatThrownBy(() -> new SecurityConfig().jwtSigningKey("too-short"))
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    void devDefaultsLiveOnlyInTheDevProfile() {
        Properties props = load("application-dev.yaml");

        assertThat(props.getProperty("app.jwt.secret")).startsWith("${JWT_SECRET:dev-only-");
    }
}
