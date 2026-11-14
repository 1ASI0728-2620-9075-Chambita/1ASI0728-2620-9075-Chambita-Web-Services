package pe.chambitalabs.platform.shared.domain.exceptions;

/**
 * The request data is invalid for the operation (400).
 */
public class BadRequestException extends DomainException {

    public BadRequestException(String messageKey, Object... arguments) {
        super(messageKey, arguments);
    }
}
