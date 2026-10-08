package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.LESSON_DAY;
import static seedu.address.logic.commands.CommandTestUtil.LESSON_END;
import static seedu.address.logic.commands.CommandTestUtil.LESSON_START;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LESSON_SLOT;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LESSON_SLOT_TEXT;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class WeeklyLessonSlotTest {
    @Test
    public void constructor_validString_preservesDayAndTimes() {
        WeeklyLessonSlot slot = new WeeklyLessonSlot(VALID_LESSON_SLOT_TEXT);
        assertEquals(DayOfWeek.WEDNESDAY, slot.getDay());
        assertEquals(LocalTime.of(13, 0), slot.getStart());
        assertEquals(LocalTime.of(17, 0), slot.getEnd());
        assertEquals(VALID_LESSON_SLOT, slot);
    }

    @Test
    public void constructor_nullValues_throwsNullPointerException() {
        LocalTime start = LocalTime.of(13, 0);
        LocalTime end = LocalTime.of(17, 0);
        assertThrows(NullPointerException.class, () -> new WeeklyLessonSlot(null));
        assertThrows(NullPointerException.class, () -> new WeeklyLessonSlot(null, start, end));
        assertThrows(NullPointerException.class, () -> new WeeklyLessonSlot(DayOfWeek.WEDNESDAY, null, end));
        assertThrows(NullPointerException.class, () -> new WeeklyLessonSlot(DayOfWeek.WEDNESDAY, start, null));
    }

    @Test
    public void constructor_zeroHour_throwsIllegalArgumentException() {
        assertThrows(DateTimeParseException.class, () -> new WeeklyLessonSlot("Wed 0.00am 2.00am"));
        assertThrows(DateTimeParseException.class, () -> new WeeklyLessonSlot("Wed 0.00pm 2.00pm"));
    }

    @Test
    public void constructor_subMinuteTimes_throwsIllegalArgumentException() {
        LocalTime start = LocalTime.of(13, 0);
        LocalTime end = LocalTime.of(17, 0);
        assertThrows(IllegalArgumentException.class, WeeklyLessonSlot.MESSAGE_CONSTRAINTS, () ->
                new WeeklyLessonSlot(DayOfWeek.WEDNESDAY, start.plusNanos(1), end));
        assertThrows(IllegalArgumentException.class, WeeklyLessonSlot.MESSAGE_CONSTRAINTS, () ->
                new WeeklyLessonSlot(DayOfWeek.WEDNESDAY, start, end.plusSeconds(1)));
        assertThrows(IllegalArgumentException.class, WeeklyLessonSlot.MESSAGE_CONSTRAINTS, () ->
                new WeeklyLessonSlot(DayOfWeek.WEDNESDAY, start, end.plusNanos(1)));
    }

    @Test
    public void isValidWeeklyLessonSlot_invalidStrings_returnsFalse() {
        for (String input : List.of("", " ", "bad", "Funday " + LESSON_START + " " + LESSON_END,
                LESSON_DAY + " " + LESSON_START, VALID_LESSON_SLOT_TEXT + " extra",
                "extra " + VALID_LESSON_SLOT_TEXT, LESSON_DAY + " " + LESSON_START + " " + LESSON_START,
                LESSON_DAY + " " + LESSON_END + " " + LESSON_START)) {
            assertFalse(WeeklyLessonSlot.isValidWeeklyLessonSlot(input), input);
        }
    }

    @Test
    public void constructor_midnightAndNoon_interpretsAmAndPm() {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("h.mma", new Locale("EN", "SG"));
        String midnight = LocalTime.MIDNIGHT.format(timeFormat);
        String noon = LocalTime.NOON.format(timeFormat);
        String beforeMidnight = LocalTime.of(23, 59).format(timeFormat);
        String morning = LESSON_DAY + " " + midnight + " " + noon;
        String afternoon = LESSON_DAY + " " + noon + " " + beforeMidnight;

        assertTrue(WeeklyLessonSlot.isValidWeeklyLessonSlot(morning));
        assertEquals(new WeeklyLessonSlot(DayOfWeek.WEDNESDAY, LocalTime.MIDNIGHT, LocalTime.NOON),
                new WeeklyLessonSlot(morning));
        assertTrue(WeeklyLessonSlot.isValidWeeklyLessonSlot(afternoon));
        assertEquals(new WeeklyLessonSlot(DayOfWeek.WEDNESDAY, LocalTime.NOON, LocalTime.of(23, 59)),
                new WeeklyLessonSlot(afternoon));
        assertFalse(WeeklyLessonSlot.isValidWeeklyLessonSlot(LESSON_DAY + " " + beforeMidnight + " " + midnight));
    }

    @Test
    public void equalsAndHashCode_compareDayStartAndEnd() {
        WeeklyLessonSlot copy = new WeeklyLessonSlot(DayOfWeek.WEDNESDAY, LocalTime.of(13, 0), LocalTime.of(17, 0));
        assertEquals(VALID_LESSON_SLOT, copy);
        assertEquals(VALID_LESSON_SLOT.hashCode(), copy.hashCode());
        assertNotEquals(VALID_LESSON_SLOT, null);
        assertNotEquals(VALID_LESSON_SLOT, VALID_LESSON_SLOT_TEXT);
        assertNotEquals(VALID_LESSON_SLOT,
                new WeeklyLessonSlot(DayOfWeek.THURSDAY, LocalTime.of(13, 0), LocalTime.of(17, 0)));
        assertNotEquals(VALID_LESSON_SLOT,
                new WeeklyLessonSlot(DayOfWeek.WEDNESDAY, LocalTime.of(13, 1), LocalTime.of(17, 0)));
        assertNotEquals(VALID_LESSON_SLOT,
                new WeeklyLessonSlot(DayOfWeek.WEDNESDAY, LocalTime.of(13, 0), LocalTime.of(17, 1)));
    }
}
