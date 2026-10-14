package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
        StudentLessonMatchesPredicate secondaryWednesdayMath = new StudentLessonMatchesPredicate(
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
        assertTrue(new StudentLessonMatchesPredicate(Optional.empty(), mathOnWednesday)
                .test(primaryWithWednesdayMath));
    }

    private static Lesson lesson(String subject, DayOfWeek day) {
        return new Lesson(new Subject(subject),
                new WeeklyLessonSlot(day, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                new HourlyRate(new BigDecimal("45")));
    }
}
