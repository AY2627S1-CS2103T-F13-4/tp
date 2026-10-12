package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;

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
        assertEquals(MONDAY_SLOT, lesson.getWeeklyLessonSlot());
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
    public void toStringMethod() {
        assertEquals("Math, " + MONDAY_SLOT + ", SGD 45.50/hour", new Lesson(MATH, MONDAY_SLOT, RATE).toString());
    }
}
