package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.person.GuardianContact;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** End-to-end command, model and file tests for the subject feature. */
public class SubjectWorkflowTest {
    private static final String ADD = "add n/Alex Tan p/91234567 e/alex@example.com a/Clementi";

    @TempDir
    public Path temporaryFolder;
    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void addEditClearSubject_persistsEachChangeAndSupportsOmission() throws Exception {
        assertTrue(logic.execute(ADD + " s/  Combined   Science  ").getFeedbackToUser()
                .contains("Subjects: Combined Science"));
        assertEquals(Set.of(new Subject("Combined Science")), reloaded().getSubjects());
        logic.execute("edit 1 s/Math");
        assertEquals(Set.of(new Subject("Math")), reloaded().getSubjects());
        logic.execute("edit 1 s/");
        assertTrue(reloaded().getSubjects().isEmpty());
        logic.execute(ADD.replace("Alex Tan", "Bobby"));
        assertTrue(model.getFilteredPersonList().get(1).getSubjects().isEmpty());
    }

    @Test
    public void invalidSubject_changesNeitherMemoryNorFile() throws Exception {
        logic.execute(ADD + " s/Math");
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String command : new String[]{"edit 1 s/123", "edit 1 s/Math s/"}) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(before, model.getAddressBook());
            assertEquals(before, storage.readAddressBook().orElseThrow());
        }
        for (String suffix : new String[]{" s/", " s/123", " s/Math/Science", " s/Math s/"}) {
            assertThrows(ParseException.class, () -> logic.execute(ADD.replace("Alex Tan", "Bobby") + suffix));
            assertEquals(before, model.getAddressBook());
            assertEquals(before, storage.readAddressBook().orElseThrow());
        }
    }

    @Test
    public void subjectAndLevel_canBeSetTogetherAndClearedIndependently() throws Exception {
        String feedback = logic.execute(ADD + " s/Math l/Primary 5").getFeedbackToUser();
        assertTrue(feedback.contains("Subjects: Math"));
        assertTrue(feedback.contains("Level: Primary 5"));
        assertEquals(Set.of(new Subject("Math")), reloaded().getSubjects());
        assertEquals(Optional.of(new SchoolingLevel("Primary 5")), reloaded().getSchoolingLevel());

        logic.execute("edit 1 l/Primary 6 s/Science");
        assertEquals(Set.of(new Subject("Science")), reloaded().getSubjects());
        assertEquals(Optional.of(new SchoolingLevel("Primary 6")), reloaded().getSchoolingLevel());
        logic.execute("edit 1 s/");
        assertTrue(reloaded().getSubjects().isEmpty());
        assertEquals(Optional.of(new SchoolingLevel("Primary 6")), reloaded().getSchoolingLevel());
        logic.execute("edit 1 s/Math l/");
        assertEquals(Set.of(new Subject("Math")), reloaded().getSubjects());
        assertTrue(reloaded().getSchoolingLevel().isEmpty());
        logic.execute("edit 1 l/Secondary 1");
        assertEquals(Set.of(new Subject("Math")), reloaded().getSubjects());
        logic.execute("edit 1 s/ l/");
        assertTrue(reloaded().getSubjects().isEmpty());
        assertTrue(reloaded().getSchoolingLevel().isEmpty());
    }

    @Test
    public void combinedInvalidInput_changesNeitherMemoryNorFile() throws Exception {
        logic.execute(ADD + " s/Math l/Primary 5");
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String command : new String[]{"edit 1 s/Science l/***", "edit 1 s/123 l/Primary 6",
            "edit 1 s/Science l/P5 l/P6", "edit 1 s/Math s/ l/P6",
            ADD.replace("Alex Tan", "Bobby") + " s/Math l/",
            ADD.replace("Alex Tan", "Bobby") + " s/ l/Primary 5"}) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(before, model.getAddressBook());
            assertEquals(before, storage.readAddressBook().orElseThrow());
        }
    }

    @Test
    public void editAfterLevelSearch_preservesOtherStudentsAndTuitionFields() throws Exception {
        Person first = new PersonBuilder().withName("Amy Bee").build();
        Person student = new PersonBuilder().withName("Bobby")
                .withSubjects(new Subject("Math"), new Subject("English"))
                .withSchoolingLevel(new SchoolingLevel("Primary 5"))
                .withGuardianContact(new GuardianContact(new Name("Janet"), new Phone("91234567")))
                .withWeeklyLessonSlot(new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0)))
                .withHourlyRate(new HourlyRate(new BigDecimal("40.50"))).build();
        model.addPerson(first);
        model.addPerson(student);
        logic.execute("find l/pRiM");
        assertEquals(List.of(student), model.getFilteredPersonList());
        logic.execute("edit 1 s/Science s/English l/Primary 6");
        Person expected = new PersonBuilder(student).withSubjects(new Subject("Science"), new Subject("English"))
                .withSchoolingLevel(new SchoolingLevel("Primary 6")).build();
        assertEquals(first, reloadedAt(0));
        assertEquals(expected, reloadedAt(1));
    }

    @Test
    public void editingSubject_preservesAllOtherFieldsAndFilteredIndex() throws Exception {
        Person first = new PersonBuilder().withName("Amy Bee").build();
        Person student = new PersonBuilder().withName("Bobby")
                .withSchoolingLevel(new SchoolingLevel("Primary 5"))
                .withGuardianContact(new GuardianContact(new Name("Janet"), new Phone("91234567")))
                .withWeeklyLessonSlot(new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0)))
                .withHourlyRate(new HourlyRate(new BigDecimal("40.50"))).build();
        model.addPerson(first);
        model.addPerson(student);
        logic.execute("find Bobby");
        logic.execute("edit 1 s/Math");
        Person expected = new PersonBuilder(student).withSubject(new Subject("Math")).build();
        assertEquals(first, model.getAddressBook().getPersonList().get(0));
        assertEquals(expected, storage.readAddressBook().orElseThrow().getPersonList().get(1));
        logic.execute("edit 2 p/88888888");
        assertEquals(Set.of(new Subject("Math")), reloadedAt(1).getSubjects());
        logic.execute("edit 2 s/");
        assertEquals(new PersonBuilder(student).withPhone("88888888").build(), reloadedAt(1));
    }

    @Test
    public void subjectDoesNotChangeIdentityOrPermitInvalidIndexes() throws Exception {
        logic.execute(ADD + " s/Math");
        assertThrows(CommandException.class, () -> logic.execute(ADD + " s/English"));
        assertThrows(CommandException.class, () -> logic.execute("edit 2 s/English"));
        assertEquals(Set.of(new Subject("Math")), reloaded().getSubjects());
    }

    @Test
    public void descriptorCopy_distinguishesOmittedSetAndCleared() {
        EditPersonDescriptor omitted = new EditPersonDescriptor();
        EditPersonDescriptor clear = new EditPersonDescriptor();
        clear.setSubjects(Set.of());
        EditPersonDescriptor set = new EditPersonDescriptor();
        set.setSubjects(Set.of(new Subject("Math")));
        assertFalse(omitted.isAnyFieldEdited());
        assertTrue(clear.isAnyFieldEdited());
        assertNotEquals(omitted, clear);
        assertNotEquals(clear, set);
        assertEquals(clear, new EditPersonDescriptor(clear));
        assertEquals(set, new EditPersonDescriptor(set));
    }

    @Test
    public void multipleSubjects_replaceRemovePreserveAndClear() throws Exception {
        String feedback = logic.execute(ADD + " s/Math s/ Combined   Science s/math").getFeedbackToUser();
        assertTrue(feedback.contains("Subjects: Math, Combined Science"));
        assertEquals(List.of(new Subject("Math"), new Subject("Combined Science")),
                List.copyOf(reloaded().getSubjects()));
        logic.execute("edit 1 p/88888888");
        assertEquals(2, reloaded().getSubjects().size());
        logic.execute("edit 1 s/English s/Physics");
        assertEquals(Set.of(new Subject("English"), new Subject("Physics")), reloaded().getSubjects());
        logic.execute("edit 1 s/Physics");
        assertEquals(Set.of(new Subject("Physics")), reloaded().getSubjects());
        logic.execute("edit 1 s/");
        assertTrue(reloaded().getSubjects().isEmpty());
        assertEquals(new Phone("88888888"), reloaded().getPhone());
    }

    @Test
    public void blankOrInvalidSubjectAmongValidValues_rejectsWholeCommand() throws Exception {
        logic.execute(ADD + " s/Math s/English");
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String fields : new String[]{" s/ s/Math", " s/Math s/   ", " s/ s/", " s/Math s/123"}) {
            assertThrows(ParseException.class, () -> logic.execute("edit 1" + fields));
            assertThrows(ParseException.class, () -> logic.execute(ADD.replace("Alex Tan", "Bobby") + fields));
            assertEquals(before, model.getAddressBook());
            assertEquals(before, storage.readAddressBook().orElseThrow());
        }
    }

    @Test
    public void subjects_areDefensivelyCopiedAndCannotBeMutatedThroughGetters() {
        Set<Subject> input = new LinkedHashSet<>(List.of(new Subject("Math"), new Subject("English")));
        Person base = new PersonBuilder().build();
        Person person = new Person(base.getName(), base.getPhone(), base.getEmail(), base.getAddress(),
                base.getTags(), input, base.getSchoolingLevel(), base.getGuardianContact(),
                base.getWeeklyLessonSlot(), base.getHourlyRate());
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.setSubjects(input);
        EditPersonDescriptor copy = new EditPersonDescriptor(descriptor);
        input.clear();
        assertEquals(2, person.getSubjects().size());
        assertEquals(person.getSubjects(), copy.getSubjects());
        assertThrows(UnsupportedOperationException.class, () -> person.getSubjects().clear());
        assertThrows(UnsupportedOperationException.class, () -> descriptor.getSubjects().clear());
        descriptor.setSubjects(Set.of());
        assertEquals(2, copy.getSubjects().size());
    }

    private Person reloaded() throws Exception {
        return reloadedAt(0);
    }

    private Person reloadedAt(int index) throws Exception {
        return storage.readAddressBook().orElseThrow().getPersonList().get(index);
    }
}
