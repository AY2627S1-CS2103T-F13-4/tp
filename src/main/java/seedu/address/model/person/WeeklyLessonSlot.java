package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** One weekly lesson, with minute precision and no overnight range. */
public final class WeeklyLessonSlot implements Comparable<WeeklyLessonSlot> {
    public static final String MESSAGE_CONSTRAINTS =
            "A lesson slot must use minute precision and end after it starts on the same day";
    public static final String MESSAGE_INVALID_FORMAT = "Lesson slots must be in the format DAY START END, "
            + "where DAY is a three-letter day (e.g. Mon, Sun) and START and END are minute-precise times "
            + "(e.g. 10.30am, 1.00pm). Example: Wed 1.00pm 5.00pm";
    public static final String MESSAGE_END_NOT_AFTER_START =
            "A lesson slot's START must be before its END on the same day, e.g. Wed 1.00pm 5.00pm";

    private static final DateTimeFormatter DAY_FORMAT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive().appendPattern("E").toFormatter(new Locale("EN", "SG"));
    private static final DateTimeFormatter TIME_FORMAT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive().appendPattern("h.mma").toFormatter(new Locale("EN", "SG"))
            .withResolverStyle(ResolverStyle.STRICT);

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
     * Returns true if a given string is written as DAY START END, whether or not END is after START.
     */
    public static boolean isValidFormat(String test) {
        Pattern pattern = Pattern.compile(VALIDATION_REGEX);
        Matcher matcher = pattern.matcher(test);

        if (!matcher.matches()) {
            return false;
        }

        try {
            DayOfWeek.from(DAY_FORMAT.parse(matcher.group(1)));
            LocalTime.parse(matcher.group(2), TIME_FORMAT);
            LocalTime.parse(matcher.group(3), TIME_FORMAT);
        } catch (DateTimeException e) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if a given string is a valid weekly lesson slot.
     */
    public static boolean isValidWeeklyLessonSlot(String test) {
        if (!isValidFormat(test)) {
            return false;
        }

        Matcher matcher = Pattern.compile(VALIDATION_REGEX).matcher(test);
        matcher.matches();
        LocalTime start = LocalTime.parse(matcher.group(2), TIME_FORMAT);
        LocalTime end = LocalTime.parse(matcher.group(3), TIME_FORMAT);
        return end.isAfter(start);
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

    /**
     * Compares slots by day (Monday to Sunday), then start time, then end time.
     */
    @Override
    public int compareTo(WeeklyLessonSlot other) {
        requireNonNull(other);
        int dayComparison = day.compareTo(other.day);
        if (dayComparison != 0) {
            return dayComparison;
        }
        int startComparison = start.compareTo(other.start);
        if (startComparison != 0) {
            return startComparison;
        }
        return end.compareTo(other.end);
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

    /** Returns {@code time} the way lesson slots are written, e.g. "9.00am" or "4.30pm". */
    public static String formatTime(LocalTime time) {
        return time.format(TIME_FORMAT);
    }

    @Override
    public String toString() {
        return day.getDisplayName(TextStyle.FULL, new Locale("EN", "SG")) + " " + start.format(TIME_FORMAT) + "-"
                + end.format(TIME_FORMAT);
    }
}
