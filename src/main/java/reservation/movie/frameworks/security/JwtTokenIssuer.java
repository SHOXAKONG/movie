package reservation.movie.frameworks.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;
import reservation.movie.entities.User;
import reservation.movie.usecases.ports.output.auth.ITokenIssuer;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Component
public class JwtTokenIssuer implements ITokenIssuer {

    static final String ISSUER = "movie-reservation";

    private final JwtEncoder encoder;
    private final Duration ttl;
    private final Clock clock;

    public JwtTokenIssuer(JwtEncoder encoder, @Value("${app.jwt.ttl:PT2H}") Duration ttl, Clock clock) {
        this.encoder = encoder;
        this.ttl = ttl;
        this.clock = clock;
    }

    @Override
    public IssuedToken issue(User user) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(ttl);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("email", user.getEmail())
                .claim(SecurityConfig.ROLE_CLAIM, user.getRole().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, expiresAt);
    }
}
