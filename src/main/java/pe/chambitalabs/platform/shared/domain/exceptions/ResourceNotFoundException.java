package pe.chambitalabs.platform.shared.domain.exceptions;

/**
 * Requested resource does not exist (404).
 */
public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String messageKey, Object... arguments) {
        super(messageKey, arguments);
    }
}
