package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class LessonFilterPredicateTest {

    private static final Lesson TUESDAY_MATH = lesson("Math", DayOfWeek.TUESDAY);
    private static final Lesson WEDNESDAY_ENGLISH = lesson("English", DayOfWeek.WEDNESDAY);
    private static final Lesson WEDNESDAY_MATH = lesson("Mathematics", DayOfWeek.WEDNESDAY);

    @Test
    public void lessonMatch_combinesSubjectAndDayOnOneLesson() {
        LessonMatchesPredicate mathOnWednesday = new LessonMatchesPredicate(
                Optional.of("mAtH"), Optional.of(DayOfWeek.WEDNESDAY));

        assertFalse(mathOnWednesday.test(TUESDAY_MATH));
        assertFalse(mathOnWednesday.test(WEDNESDAY_ENGLISH));
        assertTrue(mathOnWednesday.test(WEDNESDAY_MATH));
        assertTrue(new LessonMatchesPredicate(Optional.empty(), Optional.of(DayOfWeek.TUESDAY))
                .test(TUESDAY_MATH));
        assertTrue(new LessonMatchesPredicate(Optional.of("ENG"), Optional.empty())
                .test(WEDNESDAY_ENGLISH));
    }

    @Test
    public void studentMatch_requiresOneQualifyingLessonAndMatchingLevel() {
        LessonMatchesPredicate mathOnWednesday = new LessonMatchesPredicate(
                Optional.of("math"), Optional.of(DayOfWeek.WEDNESDAY));
        StudentSearchPredicate secondaryWednesdayMath = studentSearch(
                Optional.of("secondary"), mathOnWednesday);
        Student secondaryWithoutLessons = new PersonBuilder()
                .withSchoolingLevel(new SchoolingLevel("Secondary 3")).build();
        Student secondaryWithSeparateMatches = new PersonBuilder()
                .withSchoolingLevel(new SchoolingLevel("Secondary 3"))
                .withLessons(TUESDAY_MATH, WEDNESDAY_ENGLISH).build();
        Student secondaryWithWednesdayMath = new PersonBuilder()
                .withSchoolingLevel(new SchoolingLevel("Secondary 3"))
                .withLessons(WEDNESDAY_MATH).build();
        Student primaryWithWednesdayMath = new PersonBuilder()
                .withSchoolingLevel(new SchoolingLevel("Primary 5"))
                .withLessons(WEDNESDAY_MATH).build();

        assertFalse(secondaryWednesdayMath.test(secondaryWithoutLessons));
        assertFalse(secondaryWednesdayMath.test(secondaryWithSeparateMatches));
        assertTrue(secondaryWednesdayMath.test(secondaryWithWednesdayMath));
        assertFalse(secondaryWednesdayMath.test(primaryWithWednesdayMath));
        assertTrue(studentSearch(Optional.empty(), mathOnWednesday)
                .test(primaryWithWednesdayMath));
    }

    @Test
    public void filterValueEquality_distinguishesCriteria() {
        LessonMatchesPredicate wednesdayMath = new LessonMatchesPredicate(
                Optional.of("Math"), Optional.of(DayOfWeek.WEDNESDAY));
        LessonMatchesPredicate sameLessonFilter = new LessonMatchesPredicate(
                Optional.of("math"), Optional.of(DayOfWeek.WEDNESDAY));
        LessonMatchesPredicate tuesdayMath = new LessonMatchesPredicate(
                Optional.of("Math"), Optional.of(DayOfWeek.TUESDAY));
        assertEquals(wednesdayMath, sameLessonFilter);
        assertEquals(wednesdayMath.hashCode(), sameLessonFilter.hashCode());
        assertNotEquals(wednesdayMath, tuesdayMath);

        StudentSearchPredicate secondaryMath = studentSearch(
                Optional.of("Secondary"), wednesdayMath);
        StudentSearchPredicate sameStudentFilter = studentSearch(
                Optional.of("secondary"), sameLessonFilter);
        assertEquals(secondaryMath, sameStudentFilter);
        assertEquals(secondaryMath.hashCode(), sameStudentFilter.hashCode());
        assertNotEquals(secondaryMath, studentSearch(Optional.of("Primary"), wednesdayMath));
    }

    private static StudentSearchPredicate studentSearch(Optional<String> level, LessonMatchesPredicate lesson) {
        return new StudentSearchPredicate(Optional.empty(), Optional.empty(), Optional.empty(), level,
                Optional.of(lesson));
    }

    private static Lesson lesson(String subject, DayOfWeek day) {
        return new Lesson(new Subject(subject),
                new WeeklyLessonSlot(day, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                new HourlyRate(new BigDecimal("45")));
    }
}
