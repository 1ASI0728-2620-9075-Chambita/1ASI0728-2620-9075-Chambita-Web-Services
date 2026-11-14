package pe.chambitalabs.platform.iam.infrastructure.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pe.chambitalabs.platform.iam.application.internal.outboundservices.MailService;

/**
 * Mail Gateway: sends emails with Spring Mail through SMTP. When no SMTP host is configured
 * (local development), the message is written to the log instead.
 */
@Service
public class SmtpMailService implements MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpMailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;

    public SmtpMailService(ObjectProvider<JavaMailSender> mailSender,
                           @Value("${chambita.mail.from:no-reply@chambita.pe}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendVerificationCode(String to, String fullName, String code) {
        var subject = "Chambita · Tu código de verificación / Your verification code";
        var text = """
                Hola, %s:

                Tu código para verificar tu correo institucional es: %s
                Vence en 15 minutos.

                Your code to verify your institutional email is: %s
                It expires in 15 minutes.

                Chambita
                """.formatted(fullName, code, code);
        var sender = mailSender.getIfAvailable();
        if (sender == null) {
            LOGGER.info("SMTP not configured. Verification code for {}: {}", to, code);
            return;
        }
        var message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        sender.send(message);
    }
}
