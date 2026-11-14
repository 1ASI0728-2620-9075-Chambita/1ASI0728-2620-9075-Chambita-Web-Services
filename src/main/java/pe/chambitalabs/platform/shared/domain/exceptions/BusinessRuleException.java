package pe.chambitalabs.platform.shared.domain.exceptions;

/**
 * Request breaks a business rule (422).
 */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String messageKey, Object... arguments) {
        super(messageKey, arguments);
    }
}
