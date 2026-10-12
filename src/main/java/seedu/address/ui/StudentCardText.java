package seedu.address.ui;

import java.util.Optional;
import java.util.stream.Collectors;

import seedu.address.model.person.Person;

/**
 * Formats a student's optional details for {@code PersonCard}.
 * Each method returns empty when the detail has not been recorded, so the card can show a muted placeholder.
 */
final class StudentCardText {

    static final String NOT_SET = "Not set";
    static final String NO_SUBJECTS = "No subjects yet";

    private StudentCardText() {}

    /** Returns the student's subjects in their stored order, separated by commas. */
    static Optional<String> subjects(Person person) {
        if (person.getSubjects().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(person.getSubjects().stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    /** Returns the weekly lesson slot, e.g. "Tuesday 4.00pm-5.30pm". */
    static Optional<String> lesson(Person person) {
        return person.getWeeklyLessonSlot().map(Object::toString);
    }

    /** Returns the hourly rate, e.g. "SGD 60.00/hour". */
    static Optional<String> rate(Person person) {
        return person.getHourlyRate().map(rate -> "SGD " + rate + "/hour");
    }

    /** Returns the guardian's name and phone, e.g. "Janet Tan (91234567)". */
    static Optional<String> guardian(Person person) {
        return person.getGuardianContact().map(guardian -> guardian.getName() + " (" + guardian.getPhone() + ")");
    }
}
