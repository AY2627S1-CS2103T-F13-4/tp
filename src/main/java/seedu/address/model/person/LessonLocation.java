package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/** An immutable, normalized place where a lesson is held, such as an address, room or meeting link. */
public final class LessonLocation {
    public static final String MESSAGE_CONSTRAINTS =
            "Lesson locations must contain 1-100 characters and must not contain a word of letters "
            + "followed by '/' (such as 'x/'), which would look like a command prefix";
    public final String value;

    /** Creates a validated location, trimming and collapsing spaces and tabs. */
    public LessonLocation(String text) {
        requireNonNull(text);
        checkArgument(isValidLocation(text), MESSAGE_CONSTRAINTS);
        value = normalize(text);
    }

    /** Returns whether the normalized input is a valid location. */
    public static boolean isValidLocation(String text) {
        requireNonNull(text);
        String normalized = normalize(text);
        return !normalized.isEmpty() && normalized.length() <= 100 && !normalized.matches(".*(?:^|\\s)[A-Za-z]+/.*");
    }

    private static String normalize(String text) {
        return text.replaceAll("^[ \t]+|[ \t]+$", "").replaceAll("[ \t]+", " ");
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LessonLocation location && value.equalsIgnoreCase(location.value);
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
