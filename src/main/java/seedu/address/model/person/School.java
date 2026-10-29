package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/** Represents the school a student attends. */
public final class School {

    public static final String MESSAGE_CONSTRAINTS = "Schools must contain 1-60 characters and must not be blank";

    public final String value;

    /** Creates a validated school, trimming leading and trailing whitespace. */
    public School(String text) {
        requireNonNull(text);
        checkArgument(isValidSchool(text), MESSAGE_CONSTRAINTS);
        value = text.trim();
    }

    /** Returns whether the trimmed input is a valid school. */
    public static boolean isValidSchool(String text) {
        requireNonNull(text);
        String trimmed = text.trim();
        return !trimmed.isBlank() && trimmed.length() <= 60;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof School school && value.equals(school.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
