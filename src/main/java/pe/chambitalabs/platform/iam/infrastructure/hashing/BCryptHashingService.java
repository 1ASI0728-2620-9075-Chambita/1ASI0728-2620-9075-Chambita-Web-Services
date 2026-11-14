package pe.chambitalabs.platform.iam.infrastructure.hashing;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pe.chambitalabs.platform.iam.application.internal.outboundservices.HashingService;

/** Implements HashingService with BCrypt. */
@Service
public class BCryptHashingService implements HashingService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence raw) {
        return encoder.encode(raw);
    }

    @Override
    public boolean matches(CharSequence raw, String hash) {
        return encoder.matches(raw, hash);
    }
}
