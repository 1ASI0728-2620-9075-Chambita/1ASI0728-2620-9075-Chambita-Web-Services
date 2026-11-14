package pe.chambitalabs.platform.iam.infrastructure.tokens;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.chambitalabs.platform.iam.application.internal.outboundservices.TokenService;
import pe.chambitalabs.platform.iam.domain.model.aggregates.User;
import pe.chambitalabs.platform.shared.infrastructure.security.AuthenticatedUser;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/** Implements TokenService with signed JWT (HS256). */
@Service
public class JwtTokenService implements TokenService {

    private final SecretKey key;
    private final Duration expiration;

    public JwtTokenService(@Value("${chambita.security.jwt.secret}") String base64Secret,
                           @Value("${chambita.security.jwt.expiration-hours:24}") long expirationHours) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.expiration = Duration.ofHours(expirationHours);
    }

    @Override
    public String generateToken(User user) {
        var now = Instant.now();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail().value())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(key)
                .compact();
    }

    @Override
    public Optional<AuthenticatedUser> validateToken(String token) {
        try {
            var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return Optional.of(new AuthenticatedUser(UUID.fromString(claims.getSubject()),
                    claims.get("email", String.class), claims.get("role", String.class)));
        } catch (JwtException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
