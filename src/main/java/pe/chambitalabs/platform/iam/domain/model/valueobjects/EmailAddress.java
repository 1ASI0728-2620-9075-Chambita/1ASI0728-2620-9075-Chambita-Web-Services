package pe.chambitalabs.platform.iam.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Locale;
import java.util.regex.Pattern;

/** Validated, lower-case email address. */
@Embeddable
public record EmailAddress(@Column(name = "email", nullable = false, unique = true, length = 160) String value) {

    private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public EmailAddress {
        if (value == null || !FORMAT.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
    }

    public String domain() {
        return value.substring(value.indexOf('@') + 1);
    }
}
