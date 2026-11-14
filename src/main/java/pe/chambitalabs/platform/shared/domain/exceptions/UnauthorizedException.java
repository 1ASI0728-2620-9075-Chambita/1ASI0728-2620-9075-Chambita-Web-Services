package pe.chambitalabs.platform.shared.domain.exceptions;

/**
 * Credentials are missing or invalid (401).
 */
public class UnauthorizedException extends DomainException {

    public UnauthorizedException(String messageKey, Object... arguments) {
        super(messageKey, arguments);
    }
}
