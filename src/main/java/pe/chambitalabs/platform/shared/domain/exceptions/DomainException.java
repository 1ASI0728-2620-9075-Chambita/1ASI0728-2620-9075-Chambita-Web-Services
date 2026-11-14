package pe.chambitalabs.platform.shared.domain.exceptions;

/**
 * Base exception that carries a message key resolved in en_US or es_419 by the API.
 */
public abstract class DomainException extends RuntimeException {

    private final String messageKey;
    private final transient Object[] arguments;

    protected DomainException(String messageKey, Object... arguments) {
        super(messageKey);
        this.messageKey = messageKey;
        this.arguments = arguments;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArguments() {
        return arguments;
    }
}
