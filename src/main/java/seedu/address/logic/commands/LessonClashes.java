package seedu.address.logic.commands;

import java.util.ArrayList;
import java.util.List;

import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;

/** Finds conflicts in the complete student list without changing records. */
public final class LessonClashes {
    private LessonClashes() {}

    /** Returns a warning for the changed lesson, excluding that lesson itself. */
    public static String warning(List<Student> students, Student owner, int lessonPosition) {
        Lesson changed = owner.getLessons().get(lessonPosition);
        List<String> conflicts = new ArrayList<>();
        for (Student student : students) {
            for (int i = 0; i < student.getLessons().size(); i++) {
                if (student.equals(owner) && i == lessonPosition) {
                    continue;
                }
                Lesson other = student.getLessons().get(i);
                if (changed.getDay() == other.getDay() && changed.getStart().isBefore(other.getEnd())
                        && other.getStart().isBefore(changed.getEnd())) {
                    conflicts.add(student.getName() + ", lesson " + (i + 1) + " (" + other.getSubject()
                            + ", " + other.getWeeklyLessonSlot() + ")");
                }
            }
        }
        return conflicts.isEmpty() ? "" : "\nWarning: Lesson " + (lessonPosition + 1) + " for " + owner.getName()
                + " overlaps with " + conflicts.size() + " lesson(s):\n- " + String.join("\n- ", conflicts)
                + "\nThe change was applied.";
    }
}
