package pe.chambitalabs.platform.shared.domain.exceptions;

/**
 * Authenticated user cannot operate on the resource (403).
 */
public class ForbiddenException extends DomainException {

    public ForbiddenException(String messageKey, Object... arguments) {
        super(messageKey, arguments);
    }
}
