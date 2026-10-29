package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;

/** Finds conflicts in the complete student list and creates feedback without changing records. */
public final class LessonClashes {
    public static final String MESSAGE_CLASH_WARNING = "Lesson saved - scheduling clash detected";
    public static final String MESSAGE_CONFLICT = "%1$s, lesson %2$d (%3$s, %4$s)";
    public static final String WARNING_CONFLICT = "\nWarning: Lesson %1$d for %2$s overlaps with %3$d lesson(s):%4$s";

    private LessonClashes() {}

    /** Creates feedback for one changed lesson, using the saved-lesson heading when clashes exist. */
    public static CommandResult resultForLesson(ReadOnlyAddressBook addressBook, Student owner,
            int lessonPosition, String successMessage) {
        requireAllNonNull(addressBook, owner, successMessage);
        String warnings = warning(studentsFor(addressBook, owner), owner, lessonPosition);
        return buildResult(successMessage, MESSAGE_CLASH_WARNING, warnings);
    }

    /** Creates feedback for all of a student's lessons, retaining the success heading when clashes exist. */
    public static CommandResult resultForStudent(ReadOnlyAddressBook addressBook, Student owner,
            String successMessage) {
        requireAllNonNull(addressBook, owner, successMessage);
        List<Student> students = studentsFor(addressBook, owner);
        StringBuilder warnings = new StringBuilder();
        for (int i = 0; i < owner.getLessons().size(); i++) {
            warnings.append(warning(students, owner, i));
        }
        return buildResult(successMessage, successMessage, warnings.toString());
    }

    private static List<Student> studentsFor(ReadOnlyAddressBook addressBook, Student owner) {
        return owner.isArchived() ? addressBook.getArchivedPersonList() : addressBook.getPersonList();
    }

    private static CommandResult buildResult(String successMessage, String warningHeading, String warnings) {
        return warnings.isEmpty()
                ? new CommandResult(successMessage) : CommandResult.warning(warningHeading + warnings);
    }

    /** Returns a warning for the changed lesson, excluding that lesson itself. */
    public static String warning(List<Student> students, Student owner, int lessonPosition) {
        Lesson changed = owner.getLessons().get(lessonPosition);
        List<String> conflicts = new ArrayList<>();
        for (Student student : students) {
            List<Lesson> lessons = student.getLessons();
            for (int i = 0; i < lessons.size(); i++) {
                Lesson other = lessons.get(i);
                if (student.equals(owner) && i == lessonPosition) {
                    continue;
                }
                if (!changed.overlapsWith(other)) {
                    continue;
                }

                conflicts.add(String.format(MESSAGE_CONFLICT, student.getName(), i + 1, other.getSubject(),
                        other.getLessonSlot()));

            }
        }

        return conflicts.isEmpty() ? "" : String.format(WARNING_CONFLICT, lessonPosition + 1, owner.getName(),
                conflicts.size(), "\n- " + String.join("\n- ", conflicts));
    }
}
