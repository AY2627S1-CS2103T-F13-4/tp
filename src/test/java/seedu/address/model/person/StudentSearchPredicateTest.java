package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class StudentSearchPredicateTest {
    private static final Lesson MONDAY_MATH = new Lesson(new Subject("Math"),
            new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0)),
            new HourlyRate(new BigDecimal("45")));
    private static final LessonMatchesPredicate MATH = new LessonMatchesPredicate(
            Optional.of("math"), Optional.empty());
    private static final Student ALEX = new PersonBuilder().withName("Alex Tan")
            .withGuardian(new Guardian(new Name("Janet Lee"), new Phone("91234567")))
            .withSchool(new School("Raffles Institution"))
            .withSchoolingLevel(new SchoolingLevel("Secondary 3")).withLessons(MONDAY_MATH).build();

    @Test
    public void test_matchesAllCriteriaIgnoringCase() {
        assertTrue(search("aLeX", "ANET", "fFlEs Ins", "SECONDARY", MATH).test(ALEX));
        assertTrue(search("lex tan", "1234", "raffles", "3", MATH).test(ALEX));
    }

    @Test
    public void test_requiresEverySuppliedCriterion() {
        for (StudentSearchPredicate predicate : List.of(
                search("bob", "janet", "raffles", "secondary", MATH),
                search("alex", "missing", "raffles", "secondary", MATH),
                search("alex", "janet", "dunman", "secondary", MATH),
                search("alex", "janet", "raffles", "primary", MATH),
                search("alex", "janet", "raffles", "secondary",
                        new LessonMatchesPredicate(Optional.of("English"), Optional.empty())))) {
            assertFalse(predicate.test(ALEX));
        }
    }

    @Test
    public void test_missingFieldsFailOnlyWhenTheirCriteriaAreSupplied() {
        assertFalse(search(null, "janet", null, null, null)
                .test(new PersonBuilder(ALEX).withGuardian(null).build()));
        assertFalse(search(null, null, "raffles", null, null)
                .test(new PersonBuilder(ALEX).withSchool(null).build()));
        assertFalse(search(null, null, null, "secondary", null)
                .test(new PersonBuilder(ALEX).withSchoolingLevel(null).build()));
        Student bare = new PersonBuilder().withName("Alex Tan").build();
        assertTrue(search("alex", null, null, null, null).test(bare));
        assertTrue(search(null, null, null, null, null).test(bare));
        assertFalse(search(null, null, null, null, MATH).test(bare));
    }

    @Test
    public void test_contactAndSchoolCriteriaDoNotRequireLessons() {
        Student noLessons = new PersonBuilder(ALEX).withLessons().build();
        assertTrue(search("alex", "janet", "raffles", "secondary", null).test(noLessons));
        assertFalse(search("alex", "janet", "raffles", "secondary", MATH).test(noLessons));
        Student ownPhoneOnly = new PersonBuilder().withPhone("91234567").build();
        assertFalse(search(null, "1234", null, null, null).test(ownPhoneOnly));
    }

    @Test
    public void equals_comparesEveryCriterionIgnoringTextCase() {
        StudentSearchPredicate predicate = search("Alex", "Janet", "Raffles", "Secondary", MATH);
        StudentSearchPredicate equivalent = search("ALEX", "JANET", "RAFFLES", "SECONDARY",
                new LessonMatchesPredicate(Optional.of("MATH"), Optional.empty()));
        assertEquals(predicate, predicate);
        assertEquals(predicate, equivalent);
        assertEquals(equivalent, predicate);
        assertEquals(predicate.hashCode(), equivalent.hashCode());
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, "Alex");
        for (StudentSearchPredicate other : List.of(
                search("Bob", "Janet", "Raffles", "Secondary", MATH),
                search("Alex", "John", "Raffles", "Secondary", MATH),
                search("Alex", "Janet", "Dunman", "Secondary", MATH),
                search("Alex", "Janet", "Raffles", "Primary", MATH),
                search("Alex", "Janet", "Raffles", "Secondary", null))) {
            assertNotEquals(predicate, other);
        }
    }

    @Test
    public void constructor_nullOptionals_throwsNullPointerException() {
        Optional<String> empty = Optional.empty();
        assertThrows(NullPointerException.class, () -> new StudentSearchPredicate(
                null, empty, empty, empty, Optional.empty()));
        assertThrows(NullPointerException.class, () -> new StudentSearchPredicate(
                empty, null, empty, empty, Optional.empty()));
        assertThrows(NullPointerException.class, () -> new StudentSearchPredicate(
                empty, empty, null, empty, Optional.empty()));
        assertThrows(NullPointerException.class, () -> new StudentSearchPredicate(
                empty, empty, empty, null, Optional.empty()));
        assertThrows(NullPointerException.class, () -> new StudentSearchPredicate(
                empty, empty, empty, empty, null));
        assertThrows(NullPointerException.class, () -> search(null, null, null, null, null).test(null));
    }

    @Test
    public void toString_includesNormalizedCriteria() {
        String expected = StudentSearchPredicate.class.getCanonicalName()
                + "{name=Optional[alex], guardian=Optional[janet], school=Optional[raffles], "
                + "level=Optional.empty, lesson=Optional.empty}";
        assertEquals(expected, search("ALEX", "JANET", "RAFFLES", null, null).toString());
    }

    private static StudentSearchPredicate search(String name, String guardian, String school, String level,
            LessonMatchesPredicate lesson) {
        return new StudentSearchPredicate(Optional.ofNullable(name), Optional.ofNullable(guardian),
                Optional.ofNullable(school), Optional.ofNullable(level), Optional.ofNullable(lesson));
    }
}
