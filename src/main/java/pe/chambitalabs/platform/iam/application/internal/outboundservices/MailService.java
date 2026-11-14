package pe.chambitalabs.platform.iam.application.internal.outboundservices;

/** Port to the Mail Gateway. */
public interface MailService {

    void sendVerificationCode(String to, String fullName, String code);
}
