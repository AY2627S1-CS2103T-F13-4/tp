//@@author minweed
package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class LessonTest {

    private static final Subject MATH = new Subject("Math");
    private static final WeeklyLessonSlot MONDAY_SLOT = new WeeklyLessonSlot(DayOfWeek.MONDAY,
            LocalTime.of(13, 0), LocalTime.of(17, 0));
    private static final HourlyRate RATE = new HourlyRate(new BigDecimal("45.50"));

    @Test
    public void constructor_nullField_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Lesson(null, MONDAY_SLOT, RATE));
        assertThrows(NullPointerException.class, () -> new Lesson(MATH, null, RATE));
        assertThrows(NullPointerException.class, () -> new Lesson(MATH, MONDAY_SLOT, null));
    }

    @Test
    public void getters_returnConstructorValues() {
        Lesson lesson = new Lesson(MATH, MONDAY_SLOT, RATE);
        assertEquals(MATH, lesson.getSubject());
        assertEquals(MONDAY_SLOT, lesson.getLessonSlot());
        assertEquals(RATE, lesson.getHourlyRate());
    }

    @Test
    public void equals() {
        Lesson lesson = new Lesson(MATH, MONDAY_SLOT, RATE);

        assertTrue(lesson.equals(lesson));
        assertTrue(lesson.equals(new Lesson(new Subject("math"), new WeeklyLessonSlot(DayOfWeek.MONDAY,
                LocalTime.of(13, 0), LocalTime.of(17, 0)), new HourlyRate(new BigDecimal("45.5")))));
        assertEquals(lesson.hashCode(), new Lesson(new Subject("math"), MONDAY_SLOT, RATE).hashCode());

        assertFalse(lesson.equals(null));
        assertFalse(lesson.equals(5));
        assertNotEquals(lesson, new Lesson(new Subject("English"), MONDAY_SLOT, RATE));
        assertNotEquals(lesson, new Lesson(MATH, new WeeklyLessonSlot(DayOfWeek.TUESDAY,
                LocalTime.of(13, 0), LocalTime.of(17, 0)), RATE));
        assertNotEquals(lesson, new Lesson(MATH, MONDAY_SLOT, new HourlyRate(new BigDecimal("50"))));
    }

    @Test
    public void optionalFields_defaultEmptyAndAffectEqualityAndDisplay() {
        Lesson plain = new Lesson(MATH, MONDAY_SLOT, RATE);
        assertEquals(Optional.empty(), plain.getLocation());
        assertEquals(Optional.empty(), plain.getMode());

        Lesson full = new Lesson(MATH, MONDAY_SLOT, RATE, Optional.of(new LessonLocation("Library")),
                Optional.of(LessonMode.PHYSICAL));
        assertEquals(Optional.of(new LessonLocation("library")), full.getLocation());
        assertEquals(Optional.of(LessonMode.PHYSICAL), full.getMode());
        assertEquals(full, new Lesson(MATH, MONDAY_SLOT, RATE, Optional.of(new LessonLocation(" LIBRARY ")),
                Optional.of(LessonMode.PHYSICAL)));
        assertNotEquals(plain, full);
        assertNotEquals(full, new Lesson(MATH, MONDAY_SLOT, RATE, Optional.of(new LessonLocation("Library")),
                Optional.of(LessonMode.VIRTUAL)));
        assertNotEquals(full, new Lesson(MATH, MONDAY_SLOT, RATE, Optional.of(new LessonLocation("Home")),
                Optional.of(LessonMode.PHYSICAL)));
        assertEquals("Math, " + MONDAY_SLOT + ", SGD 45.50/hour, Physical, at Library", full.toString());
        assertEquals("Math, " + MONDAY_SLOT + ", SGD 45.50/hour, Virtual",
                new Lesson(MATH, MONDAY_SLOT, RATE, Optional.empty(), Optional.of(LessonMode.VIRTUAL)).toString());
        assertThrows(NullPointerException.class, () -> new Lesson(MATH, MONDAY_SLOT, RATE, null, Optional.empty()));
    }

    @Test
    public void lessonMode_parsing() {
        assertEquals(LessonMode.VIRTUAL, LessonMode.fromString("  ViRtUaL "));
        assertTrue(LessonMode.isValidMode("physical"));
        assertFalse(LessonMode.isValidMode(""));
        assertFalse(LessonMode.isValidMode("hybrid"));
        assertThrows(IllegalArgumentException.class, () -> LessonMode.fromString("hybrid"));
    }

    @Test
    public void lessonLocation_validation() {
        assertTrue(LessonLocation.isValidLocation("Blk 123/4 Clementi"));
        assertTrue(LessonLocation.isValidLocation("https://zoom.us/j/123"));
        assertFalse(LessonLocation.isValidLocation("   "));
        assertFalse(LessonLocation.isValidLocation("home rate/50"));
        assertFalse(LessonLocation.isValidLocation("a".repeat(101)));
        assertEquals("Clementi Library", new LessonLocation("  Clementi   Library ").toString());
    }

    @Test
    public void toStringMethod() {
        assertEquals("Math, " + MONDAY_SLOT + ", SGD 45.50/hour", new Lesson(MATH, MONDAY_SLOT, RATE).toString());
    }
}
