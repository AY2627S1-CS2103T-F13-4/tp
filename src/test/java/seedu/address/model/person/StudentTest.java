package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class StudentTest {

    private static final Lesson MONDAY_MATH = new Lesson(new Subject("Math"),
            new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(16, 0), LocalTime.of(17, 30)),
            new HourlyRate(new BigDecimal("45.50")));
    private static final Lesson TUESDAY_MATH = new Lesson(new Subject("Math"),
            new WeeklyLessonSlot(DayOfWeek.TUESDAY, LocalTime.of(16, 0), LocalTime.of(17, 30)),
            new HourlyRate(new BigDecimal("45.50")));

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Student person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name, all other attributes different -> returns true
        Student editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, all other attributes same -> returns false
        Student editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertFalse(BOB.isSamePerson(editedBob));

        // name has trailing spaces, all other attributes same -> returns false
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertFalse(BOB.isSamePerson(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Student aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Student editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void equals_lessons_comparesContentAndOrder() {
        Student withMonday = new PersonBuilder(ALICE).withLessons(MONDAY_MATH).build();
        Student withSameLesson = new PersonBuilder(ALICE).withLessons(new Lesson(new Subject("math"),
                new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(16, 0), LocalTime.of(17, 30)),
                new HourlyRate(new BigDecimal("45.5")))).build();
        Student withTuesday = new PersonBuilder(ALICE).withLessons(TUESDAY_MATH).build();

        assertEquals(withMonday, withSameLesson);
        assertEquals(withMonday.hashCode(), withSameLesson.hashCode());
        assertNotEquals(ALICE, withMonday);
        assertNotEquals(withMonday, withTuesday);
        assertEquals(ALICE, new PersonBuilder(withMonday).withLessons().build());

        Student mondayThenTuesday = new PersonBuilder(ALICE).withLessons(MONDAY_MATH, TUESDAY_MATH).build();
        Student tuesdayThenMonday = new PersonBuilder(ALICE).withLessons(TUESDAY_MATH, MONDAY_MATH).build();
        assertNotEquals(mondayThenTuesday, tuesdayThenMonday);
    }

    @Test
    public void isSamePerson_lessonsChanged_identityStillDependsOnName() {
        Student withMonday = new PersonBuilder(ALICE).withLessons(MONDAY_MATH).build();
        Student withTuesday = new PersonBuilder(ALICE).withLessons(TUESDAY_MATH).build();

        assertTrue(ALICE.isSamePerson(withMonday));
        assertTrue(withMonday.isSamePerson(withTuesday));
        assertTrue(withMonday.isSamePerson(new PersonBuilder(withMonday).withLessons().build()));
        assertFalse(withMonday.isSamePerson(new PersonBuilder(withMonday).withName(VALID_NAME_BOB).build()));
    }

    @Test
    public void personBuilder_copy_preservesLessons() {
        Student original = new PersonBuilder(ALICE).withLessons(MONDAY_MATH, TUESDAY_MATH).build();
        Student copy = new PersonBuilder(original).build();

        assertEquals(List.of(MONDAY_MATH, TUESDAY_MATH), copy.getLessons());
        assertEquals(original, copy);
    }

    @Test
    public void constructor_noLessons_isEmpty() {
        assertTrue(ALICE.getLessons().isEmpty());
    }

    @Test
    public void constructor_duplicateLessons_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, Student.MESSAGE_DUPLICATE_LESSONS, () ->
                new PersonBuilder().withLessons(MONDAY_MATH, MONDAY_MATH).build());
    }

    @Test
    public void constructor_defensivelyCopiesLessons() {
        List<Lesson> input = new java.util.ArrayList<>(List.of(MONDAY_MATH));
        Student person = new Student(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(), ALICE.getAddress(),
                ALICE.getTags(), Optional.empty(), Optional.empty(), input);
        input.clear();
        assertEquals(List.of(MONDAY_MATH), person.getLessons());
    }

    @Test
    public void getLessons_modifyList_throwsUnsupportedOperationException() {
        Student person = new PersonBuilder().withLessons(MONDAY_MATH).build();
        assertThrows(UnsupportedOperationException.class, () -> person.getLessons().clear());
    }

    @Test
    public void toStringMethod() {
        String expected = Student.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", address=" + ALICE.getAddress() + ", tags=" + ALICE.getTags()
                + ", schoolingLevel=Optional.empty, guardianContact=Optional.empty, lessons=[]}";
        assertEquals(expected, ALICE.toString());
    }
}
