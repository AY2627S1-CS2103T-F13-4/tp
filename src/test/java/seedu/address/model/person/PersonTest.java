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

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    private static final WeeklyLessonSlot MONDAY_SLOT = new WeeklyLessonSlot(DayOfWeek.MONDAY,
            LocalTime.of(16, 0), LocalTime.of(17, 30));
    private static final WeeklyLessonSlot TUESDAY_SLOT = new WeeklyLessonSlot(DayOfWeek.TUESDAY,
            LocalTime.of(16, 0), LocalTime.of(17, 30));

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, all other attributes same -> returns false
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertFalse(BOB.isSamePerson(editedBob));

        // name has trailing spaces, all other attributes same -> returns false
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertFalse(BOB.isSamePerson(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
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
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
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
    public void equals_weeklyLessonSlot_comparesPresenceAndValue() {
        Person withMondaySlot = new PersonBuilder(ALICE).withWeeklyLessonSlot(MONDAY_SLOT).build();
        Person withSameSlot = new PersonBuilder(ALICE).withWeeklyLessonSlot(new WeeklyLessonSlot(DayOfWeek.MONDAY,
                LocalTime.of(16, 0), LocalTime.of(17, 30))).build();
        Person withTuesdaySlot = new PersonBuilder(ALICE).withWeeklyLessonSlot(TUESDAY_SLOT).build();

        assertEquals(withMondaySlot, withSameSlot);
        assertEquals(withMondaySlot.hashCode(), withSameSlot.hashCode());
        assertNotEquals(ALICE, withMondaySlot);
        assertNotEquals(withMondaySlot, withTuesdaySlot);
        assertEquals(ALICE, new PersonBuilder(withMondaySlot).withWeeklyLessonSlot(null).build());
    }

    @Test
    public void isSamePerson_weeklyLessonSlotChanged_identityStillDependsOnName() {
        Person withMondaySlot = new PersonBuilder(ALICE).withWeeklyLessonSlot(MONDAY_SLOT).build();
        Person withTuesdaySlot = new PersonBuilder(ALICE).withWeeklyLessonSlot(TUESDAY_SLOT).build();

        assertTrue(ALICE.isSamePerson(withMondaySlot));
        assertTrue(withMondaySlot.isSamePerson(withTuesdaySlot));
        assertTrue(withMondaySlot.isSamePerson(new PersonBuilder(withMondaySlot).withWeeklyLessonSlot(null).build()));
        assertFalse(withMondaySlot.isSamePerson(new PersonBuilder(withMondaySlot).withName(VALID_NAME_BOB).build()));
    }

    @Test
    public void personBuilder_copy_preservesWeeklyLessonSlot() {
        Person original = new PersonBuilder(ALICE).withWeeklyLessonSlot(MONDAY_SLOT).build();
        Person copy = new PersonBuilder(original).build();

        assertEquals(Optional.of(MONDAY_SLOT), copy.getWeeklyLessonSlot());
        assertEquals(original, copy);
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName() + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail() + ", address=" + ALICE.getAddress() + ", tags=" + ALICE.getTags()
                + ", subject=Optional.empty, schoolingLevel=Optional.empty, guardianContact=Optional.empty"
                + ", weeklyLessonSlot=Optional.empty, hourlyRate=Optional.empty}";
        assertEquals(expected, ALICE.toString());
    }
}
