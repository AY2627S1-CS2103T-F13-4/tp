package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Locale;

/** How a lesson is delivered: in person or online. */
public enum LessonMode {
    PHYSICAL("Physical"),
    VIRTUAL("Virtual");

    public static final String MESSAGE_CONSTRAINTS = "Lesson mode must be either physical or virtual "
            + "(not case-sensitive)";
    private final String displayName;

    LessonMode(String displayName) {
        this.displayName = displayName;
    }

    /** Returns whether the trimmed, case-insensitive text names a mode. */
    public static boolean isValidMode(String text) {
        requireNonNull(text);
        String normalized = text.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values()).anyMatch(mode -> mode.name().equals(normalized));
    }

    /** Parses a mode, ignoring surrounding spaces and letter case. */
    public static LessonMode fromString(String text) {
        requireNonNull(text);
        if (!isValidMode(text)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        return valueOf(text.trim().toUpperCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return displayName;
    }
}
