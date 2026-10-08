package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** One weekly lesson, with minute precision and no overnight range. */
public final class WeeklyLessonSlot {
    public static final String MESSAGE_CONSTRAINTS =
            "A lesson slot must use minute precision and end after it starts on the same day.";

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("E", new Locale("EN", "SG"));
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern(
            "h.mma", new Locale("EN", "SG")).withResolverStyle(ResolverStyle.STRICT);

    private static final String VALIDATION_REGEX = "(\\S+)\\s+(\\S+)\\s+(\\S+)";

    private final DayOfWeek day;
    private final LocalTime start;
    private final LocalTime end;

    /** Creates a valid weekly lesson slot from a String. */
    public WeeklyLessonSlot(String weeklyLessonSlot) {
        requireNonNull(weeklyLessonSlot);

        Pattern pattern = Pattern.compile(VALIDATION_REGEX);
        Matcher matcher = pattern.matcher(weeklyLessonSlot);

        matcher.matches();

        String dayString = matcher.group(1);
        String startString = matcher.group(2);
        String endString = matcher.group(3);

        DayOfWeek day = DayOfWeek.from(DAY_FORMAT.parse(dayString));
        LocalTime start = LocalTime.parse(startString, TIME_FORMAT);
        LocalTime end = LocalTime.parse(endString, TIME_FORMAT);

        checkArgument(start.getSecond() == 0 && start.getNano() == 0
                && end.getSecond() == 0 && end.getNano() == 0 && end.isAfter(start), MESSAGE_CONSTRAINTS);

        this.day = day;
        this.start = start;
        this.end = end;
    }

    /** Creates a valid weekly lesson slot. */
    public WeeklyLessonSlot(DayOfWeek day, LocalTime start, LocalTime end) {
        requireAllNonNull(day, start, end);
        checkArgument(start.getSecond() == 0 && start.getNano() == 0
                && end.getSecond() == 0 && end.getNano() == 0 && end.isAfter(start), MESSAGE_CONSTRAINTS);
        this.day = day;
        this.start = start;
        this.end = end;
    }

    /**
     * Returns true if a given string is a valid weekly lesson slot.
     */
    public static boolean isValidWeeklyLessonSlot(String test) {
        Pattern pattern = Pattern.compile(VALIDATION_REGEX);
        Matcher matcher = pattern.matcher(test);

        if (!matcher.matches()) {
            return false;
        }

        String dayString = matcher.group(1);
        String startString = matcher.group(2);
        String endString = matcher.group(3);

        LocalTime start;
        LocalTime end;

        try {
            DayOfWeek.from(DAY_FORMAT.parse(dayString));
            start = LocalTime.parse(startString, TIME_FORMAT);
            end = LocalTime.parse(endString, TIME_FORMAT);
        } catch (DateTimeException e) {
            return false;
        }

        if (!end.isAfter(start)) {
            return false;
        }

        return true;
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
        return day.getDisplayName(TextStyle.FULL, new Locale("EN", "SG")) + " " + start.format(TIME_FORMAT) + "-"
                + end.format(TIME_FORMAT);
    }
}
