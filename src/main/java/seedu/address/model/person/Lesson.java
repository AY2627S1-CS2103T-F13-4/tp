package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents one recurring tuition lesson: a subject taught in a weekly time slot at an hourly rate.
 * The location and mode (physical or virtual) are optional.
 * Guarantees: required details are present and not null, immutable.
 */
public final class Lesson implements Comparable<Lesson> {
    private final Subject subject;
    private final WeeklyLessonSlot weeklyLessonSlot;
    private final HourlyRate hourlyRate;
    private final Optional<LessonLocation> location;
    private final Optional<LessonMode> mode;

    /** Creates a lesson with no location or mode. Every field must be present and not null. */
    public Lesson(Subject subject, WeeklyLessonSlot weeklyLessonSlot, HourlyRate hourlyRate) {
        this(subject, weeklyLessonSlot, hourlyRate, Optional.empty(), Optional.empty());
    }

    /** Subject, slot and rate must be present; the optionals themselves must not be null. */
    public Lesson(Subject subject, WeeklyLessonSlot weeklyLessonSlot, HourlyRate hourlyRate,
            Optional<LessonLocation> location, Optional<LessonMode> mode) {
        requireAllNonNull(subject, weeklyLessonSlot, hourlyRate, location, mode);
        this.subject = subject;
        this.weeklyLessonSlot = weeklyLessonSlot;
        this.hourlyRate = hourlyRate;
        this.location = location;
        this.mode = mode;
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

    public Optional<LessonLocation> getLocation() {
        return location;
    }

    public Optional<LessonMode> getMode() {
        return mode;
    }

    public DayOfWeek getDay() {
        return weeklyLessonSlot.getDay();
    }

    public LocalTime getStart() {
        return weeklyLessonSlot.getStart();
    }

    public LocalTime getEnd() {
        return weeklyLessonSlot.getEnd();
    }

    @Override
    public int compareTo(Lesson other) {
        requireNonNull(other);
        return weeklyLessonSlot.compareTo(other.weeklyLessonSlot);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        return other instanceof Lesson otherLesson
                && subject.equals(otherLesson.subject)
                && weeklyLessonSlot.equals(otherLesson.weeklyLessonSlot)
                && hourlyRate.equals(otherLesson.hourlyRate)
                && location.equals(otherLesson.location)
                && mode.equals(otherLesson.mode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, weeklyLessonSlot, hourlyRate, location, mode);
    }

    /**
     * Formats the lesson for display, e.g. {@code Math, Wednesday 1.00pm-5.00pm, SGD 45.50/hour},
     * followed by {@code , Physical} and {@code , at LOCATION} when those are recorded.
     */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder(subject + ", " + weeklyLessonSlot + ", SGD " + hourlyRate + "/hour");
        mode.ifPresent(m -> text.append(", ").append(m));
        location.ifPresent(l -> text.append(", at ").append(l));
        return text.toString();
    }
}
