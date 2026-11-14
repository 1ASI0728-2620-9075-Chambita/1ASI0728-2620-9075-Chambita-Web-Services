package pe.chambitalabs.platform.iam.application.internal.outboundservices;

/** Port to the password and code hashing algorithm. */
public interface HashingService {

    String encode(CharSequence raw);

    boolean matches(CharSequence raw, String hash);
}
