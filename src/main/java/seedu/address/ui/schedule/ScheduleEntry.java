package seedu.address.ui.schedule;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;

import java.time.LocalTime;

/**
 * An immutable pairing of a lesson with the student attending it.
 */
public final class ScheduleEntry implements Comparable<ScheduleEntry> {
    private final Student student;
    private final Lesson lesson;
    private int displayColumn = 0;

    /**
     * Creates an entry for the given student and lesson.
     */
    ScheduleEntry(Student student, Lesson lesson) {
        requireAllNonNull(student, lesson);
        this.student = student;
        this.lesson = lesson;
    }
    public Student getStudent() {
        return student;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public LocalTime getStart() {
        return lesson.getStart();
    }

    public LocalTime getEnd() {
        return lesson.getEnd();
    }

    public int getDisplayColumn() {
        return displayColumn;
    }

    public void setDisplayColumn(int displayColumn) {
        this.displayColumn = displayColumn;
    }

    @Override
    public int compareTo(ScheduleEntry other) {
        requireNonNull(other);
        return lesson.compareTo(other.lesson);
    }
}
