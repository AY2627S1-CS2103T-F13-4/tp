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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class StudentTest {

    private static final Phone VALID_PHONE_BOB_OBJ = new Phone(VALID_PHONE_BOB);
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
    public void isSamePerson_differentKindOfPerson_returnsFalse() {
        Guardian guardian = new Guardian(ALICE.getName(), ALICE.getPhone());
        assertFalse(ALICE.isSamePerson(guardian));
        assertFalse(guardian.isSamePerson(ALICE));
        assertFalse(ALICE.equals(guardian));
        assertTrue(guardian.isSamePerson(new Guardian(ALICE.getName(), VALID_PHONE_BOB_OBJ)));
        assertFalse(guardian.equals(new Guardian(ALICE.getName(), VALID_PHONE_BOB_OBJ)));
        assertEquals(guardian.hashCode(), new Guardian(ALICE.getName(), ALICE.getPhone()).hashCode());
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
    public void equals_school_comparesSchool() {
        School school = new School("Nanyang Primary School");
        Student withSchool = new PersonBuilder(ALICE).withSchool(school).build();
        Student sameSchool = new PersonBuilder(ALICE).withSchool(new School("Nanyang Primary School")).build();

        assertEquals(Optional.of(school), withSchool.getSchool());
        assertEquals(withSchool, sameSchool);
        assertEquals(withSchool.hashCode(), sameSchool.hashCode());
        assertNotEquals(ALICE, withSchool);
        assertNotEquals(withSchool,
                new PersonBuilder(ALICE).withSchool(new School("Clementi Primary School")).build());
        assertTrue(ALICE.isSamePerson(withSchool));
        assertEquals(withSchool, new PersonBuilder(withSchool).build());
        assertEquals(ALICE, new PersonBuilder(withSchool).withSchool(null).build());
    }

    @Test
    public void constructor_schoolAbsent_isEmpty() {
        assertTrue(ALICE.getSchool().isEmpty());
    }

    @Test
    public void constructor_nullSchool_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Student(ALICE.getName(), ALICE.getPhone(), ALICE.getEmail(),
                ALICE.getAddress(), ALICE.getTags(), null, Optional.empty(), Optional.empty(),
                List.of(), false, List.of()));
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
    public void withArchived_preservesDetailsWithoutChangingOriginal() {
        Student original = new PersonBuilder(ALICE).withSchoolingLevel(new SchoolingLevel("P5"))
                .withGuardian(new Guardian(BOB.getName(), BOB.getPhone()))
                .withLessons(MONDAY_MATH, TUESDAY_MATH).build();

        Student archived = original.withArchived(true);

        assertFalse(original.isArchived());
        assertTrue(archived.isArchived());
        assertEquals(original.getName(), archived.getName());
        assertEquals(original.getPhone(), archived.getPhone());
        assertEquals(original.getEmail(), archived.getEmail());
        assertEquals(original.getAddress(), archived.getAddress());
        assertEquals(original.getTags(), archived.getTags());
        assertEquals(original.getSchoolingLevel(), archived.getSchoolingLevel());
        assertEquals(original.getGuardian(), archived.getGuardian());
        assertEquals(original.getLessons(), archived.getLessons());
        assertEquals(original, archived.withArchived(false));
    }

    @Test
    public void equals_archiveStatus_comparesStatusWithoutChangingIdentity() {
        Student archivedAlice = ALICE.withArchived(true);
        Student archivedAliceCopy = ALICE.withArchived(true);

        assertNotEquals(ALICE, archivedAlice);
        assertTrue(ALICE.isSamePerson(archivedAlice));
        assertEquals(archivedAlice, archivedAliceCopy);
        assertEquals(archivedAlice.hashCode(), archivedAliceCopy.hashCode());
    }

    @Test
    public void personBuilder_copy_preservesArchiveStatus() {
        Student original = ALICE.withArchived(true);
        Student copy = new PersonBuilder(original).build();

        assertTrue(copy.isArchived());
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
                ALICE.getTags(), Optional.empty(), Optional.empty(), Optional.empty(), input, false, List.of());
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
                + ", school=Optional.empty, schoolingLevel=Optional.empty, guardian=Optional.empty, lessons=[]"
                + ", isArchived=false, notes=[], followUps=[]}";
        assertEquals(expected, ALICE.toString());
    }

    @Test
    public void constructor_notes_keptOldestFirstAndStableWithinADay() {
        ProgressNote october = new ProgressNote(LocalDate.of(2026, 10, 15), "Fractions");
        // Added in reverse alphabetical order, so sorting same-date notes by text would fail this test.
        ProgressNote septemberFirst = new ProgressNote(LocalDate.of(2026, 9, 1), "Set homework");
        ProgressNote septemberSecond = new ProgressNote(LocalDate.of(2026, 9, 1), "Placement test");
        Student student = new PersonBuilder(ALICE).withNotes(october, septemberFirst, septemberSecond).build();
        assertEquals(List.of(septemberFirst, septemberSecond, october), student.getNotes());
        assertThrows(UnsupportedOperationException.class, () -> student.getNotes().clear());
    }

    @Test
    public void equals_notes_compared() {
        ProgressNote note = new ProgressNote(LocalDate.of(2026, 10, 15), "Fractions");
        Student withNote = new PersonBuilder(ALICE).withNotes(note).build();
        assertEquals(withNote, new PersonBuilder(ALICE).withNotes(note).build());
        assertNotEquals(withNote, ALICE);
        assertTrue(withNote.isSamePerson(ALICE));
        assertTrue(ALICE.getNotes().isEmpty());
    }
}
