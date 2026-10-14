package seedu.address.ui.schedule;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalTime;
import java.util.Comparator;

import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;

/**
 * A lesson in the weekly calendar, with the student attending it and the numbers that commands use for both,
 * e.g. student 3, lesson 2 for {@code lesson-edit 3 2}.
 * The grid sets the display column while laying out the day; everything else is fixed.
 */
public final class ScheduleEntry implements Comparable<ScheduleEntry> {
    // Earlier lessons first; a tie keeps the list order, so each entry has one place.
    private static final Comparator<ScheduleEntry> ORDER =
            Comparator.comparing((ScheduleEntry entry) -> entry.lesson.getWeeklyLessonSlot())
                    .thenComparingInt(entry -> entry.studentNumber)
                    .thenComparingInt(entry -> entry.lessonNumber);

    private final Student student;
    private final Lesson lesson;
    private final int studentNumber;
    private final int lessonNumber;
    private int displayColumn = 0;

    /**
     * Creates an entry for {@code lesson} of {@code student}, where {@code studentNumber} is the student's
     * one-based index in the shown list and {@code lessonNumber} the lesson's one-based index.
     */
    ScheduleEntry(Student student, Lesson lesson, int studentNumber, int lessonNumber) {
        requireAllNonNull(student, lesson);
        assert studentNumber > 0 && lessonNumber > 0;
        this.student = student;
        this.lesson = lesson;
        this.studentNumber = studentNumber;
        this.lessonNumber = lessonNumber;
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

    /** Returns the label that names this lesson in commands, e.g. "#3.2" for student 3, lesson 2. */
    public String getCommandLabel() {
        return "#" + studentNumber + "." + lessonNumber;
    }

    public int getDisplayColumn() {
        return displayColumn;
    }

    void setDisplayColumn(int displayColumn) {
        this.displayColumn = displayColumn;
    }

    @Override
    public int compareTo(ScheduleEntry other) {
        requireNonNull(other);
        return ORDER.compare(this, other);
    }
}
