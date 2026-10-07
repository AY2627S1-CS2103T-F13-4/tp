package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/** One weekly lesson, with minute precision and no overnight range. */
public final class WeeklyLessonSlot {
    public static final String MESSAGE_CONSTRAINTS =
            "A lesson slot must use minute precision and end after it starts on the same day.";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private final DayOfWeek day;
    private final LocalTime start;
    private final LocalTime end;

    /** Creates a valid weekly lesson slot. */
    public WeeklyLessonSlot(DayOfWeek day, LocalTime start, LocalTime end) {
        requireAllNonNull(day, start, end);
        checkArgument(start.getSecond() == 0 && start.getNano() == 0
                && end.getSecond() == 0 && end.getNano() == 0 && end.isAfter(start), MESSAGE_CONSTRAINTS);
        this.day = day;
        this.start = start;
        this.end = end;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof WeeklyLessonSlot slot && day == slot.day
                && start.equals(slot.start) && end.equals(slot.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, start, end);
    }

    @Override
    public String toString() {
        return day + " " + start.format(TIME_FORMAT) + "-" + end.format(TIME_FORMAT);
    }
}
