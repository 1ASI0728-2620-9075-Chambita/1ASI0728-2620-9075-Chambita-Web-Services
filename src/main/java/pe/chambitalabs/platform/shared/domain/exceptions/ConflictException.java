package pe.chambitalabs.platform.shared.domain.exceptions;

/**
 * Request conflicts with the current state of the resource (409).
 */
public class ConflictException extends DomainException {

    public ConflictException(String messageKey, Object... arguments) {
        super(messageKey, arguments);
    }
}
