package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

/**
 * Represents one recurring tuition lesson: a subject taught in a weekly time slot at an hourly rate.
 * Guarantees: details are present and not null, immutable.
 */
public final class Lesson {
    private final Subject subject;
    private final WeeklyLessonSlot weeklyLessonSlot;
    private final HourlyRate hourlyRate;

    /** Every field must be present and not null. */
    public Lesson(Subject subject, WeeklyLessonSlot weeklyLessonSlot, HourlyRate hourlyRate) {
        requireAllNonNull(subject, weeklyLessonSlot, hourlyRate);
        this.subject = subject;
        this.weeklyLessonSlot = weeklyLessonSlot;
        this.hourlyRate = hourlyRate;
    }

    public Subject getSubject() {
        return subject;
    }

    public WeeklyLessonSlot getWeeklyLessonSlot() {
        return weeklyLessonSlot;
    }

    public HourlyRate getHourlyRate() {
        return hourlyRate;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        return other instanceof Lesson otherLesson
                && subject.equals(otherLesson.subject)
                && weeklyLessonSlot.equals(otherLesson.weeklyLessonSlot)
                && hourlyRate.equals(otherLesson.hourlyRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, weeklyLessonSlot, hourlyRate);
    }

    /** Formats the lesson for display, e.g. {@code Math, Wednesday 1.00PM-5.00PM, SGD 45.50/hour}. */
    @Override
    public String toString() {
        return subject + ", " + weeklyLessonSlot + ", SGD " + hourlyRate + "/hour";
    }
}
