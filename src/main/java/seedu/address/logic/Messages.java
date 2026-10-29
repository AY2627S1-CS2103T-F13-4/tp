package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Student;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The student index provided is invalid";
    public static final String MESSAGE_INVALID_LESSON_DISPLAYED_INDEX = "The lesson index provided is invalid";
    public static final String MESSAGE_INVALID_NOTE_DISPLAYED_INDEX = "The note index provided is invalid";
    public static final String MESSAGE_INVALID_FOLLOW_UP_DISPLAYED_INDEX = "The follow-up index provided is invalid";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d student(s) listed";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";
    public static final String MESSAGE_UNSUPPORTED_FIELDS =
                "The following prefix(es) are not fields of this command: ";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Returns an error message indicating the prefixes that do not belong to the command.
     */
    public static String getErrorMessageForUnsupportedPrefixes(Prefix... unsupportedPrefixes) {
        assert unsupportedPrefixes.length > 0;

        return MESSAGE_UNSUPPORTED_FIELDS + Stream.of(unsupportedPrefixes).map(Prefix::toString)
                .collect(Collectors.joining(" "));
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Student person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail())
                .append("; Address: ")
                .append(person.getAddress())
                .append("; Tags: ");
        person.getTags().forEach(builder::append);
        person.getSchool().ifPresent(school -> builder.append("; School: ").append(school));
        person.getSchoolingLevel().ifPresent(level -> builder.append("; Level: ").append(level));
        if (!person.getLessons().isEmpty()) {
            builder.append("; Lessons: ").append(person.getLessons().stream()
                    .map(Object::toString).collect(Collectors.joining(" | ")));
        }
        person.getGuardian().ifPresent(guardian -> builder.append("; Guardian: ").append(guardian));
        return builder.toString();
    }

}
