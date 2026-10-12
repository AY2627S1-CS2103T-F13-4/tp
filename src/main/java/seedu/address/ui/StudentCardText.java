package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;

/**
 * Formats a student's lessons and optional details for {@code PersonCard}.
 */
final class StudentCardText {

    static final String NOT_SET = "Not set";
    static final String NO_LESSONS = "No lessons yet";

    private StudentCardText() {}

    /**
     * Returns one line per lesson, numbered from 1 in stored order so the numbers match {@code LESSON_INDEX},
     * e.g. "1. Math · Tuesday 4.00pm-5.30pm · SGD 60.00/hour · Online · at Library".
     * Returns an empty list when the student has no lessons.
     */
    static List<String> lessons(Student student) {
        List<String> lines = new ArrayList<>();
        List<Lesson> lessons = student.getLessons();
        for (int i = 0; i < lessons.size(); i++) {
            Lesson lesson = lessons.get(i);
            StringBuilder line = new StringBuilder((i + 1) + ". " + lesson.getSubject() + " · "
                    + lesson.getWeeklyLessonSlot() + " · SGD " + lesson.getHourlyRate() + "/hour");
            lesson.getMode().ifPresent(mode -> line.append(" · ").append(mode));
            lesson.getLocation().ifPresent(location -> line.append(" · at ").append(location));
            lines.add(line.toString());
        }
        return lines;
    }

    /** Returns the guardian's name and phone, e.g. "Janet Tan (91234567)", or empty if none is recorded. */
    static Optional<String> guardian(Student student) {
        return student.getGuardian().map(guardian -> guardian.getName() + " (" + guardian.getPhone() + ")");
    }
}
