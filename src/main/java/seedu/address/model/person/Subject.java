package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/** An immutable, normalized tuition label. */
public final class Subject {
    public static final String MESSAGE_CONSTRAINTS =
            "Subjects must contain 1-40 characters, start with a letter or digit, include a letter, "
            + "and use only letters, digits, spaces, apostrophes, hyphens, periods or parentheses.";
    private static final String VALIDATION_REGEX = "[A-Za-z0-9][A-Za-z0-9 '.()\\-]*";
    public final String value;

    /** Creates a validated label, trimming and collapsing spaces and tabs. */
    public Subject(String text) {
        requireNonNull(text);
        String normalized = normalize(text);
        checkArgument(isValidSubject(text), MESSAGE_CONSTRAINTS);
        value = normalized;
    }

    /** Returns whether the normalized input is a valid label. */
    public static boolean isValidSubject(String text) {
        requireNonNull(text);
        String normalized = normalize(text);
        return normalized.length() <= 40 && normalized.matches(VALIDATION_REGEX)
                && normalized.matches(".*[A-Za-z].*");
    }

    private static String normalize(String text) {
        return text.replaceAll("^[ \t]+|[ \t]+$", "").replaceAll("[ \t]+", " ");
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Subject label && value.equalsIgnoreCase(label.value);
    }

    @Override
    public int hashCode() {
        return value.toLowerCase(Locale.ROOT).hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
