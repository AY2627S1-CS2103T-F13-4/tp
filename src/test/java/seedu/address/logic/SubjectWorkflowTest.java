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
import java.util.Optional;

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
                .contains("Subject: Combined Science"));
        assertEquals(Optional.of(new Subject("Combined Science")), reloaded().getSubject());
        logic.execute("edit 1 s/Math");
        assertEquals(Optional.of(new Subject("Math")), reloaded().getSubject());
        logic.execute("edit 1 s/");
        assertTrue(reloaded().getSubject().isEmpty());
        logic.execute(ADD.replace("Alex Tan", "Bobby"));
        assertTrue(model.getFilteredPersonList().get(1).getSubject().isEmpty());
    }

    @Test
    public void invalidOrRepeatedSubject_changesNeitherMemoryNorFile() throws Exception {
        logic.execute(ADD + " s/Math");
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String command : new String[]{"edit 1 s/123", "edit 1 s/Math s/Science"}) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(before, model.getAddressBook());
            assertEquals(before, storage.readAddressBook().orElseThrow());
        }
        for (String suffix : new String[]{" s/", " s/123", " s/Math/Science", " s/Math s/Science"}) {
            assertThrows(ParseException.class, () -> logic.execute(ADD.replace("Alex Tan", "Bobby") + suffix));
            assertEquals(before, model.getAddressBook());
            assertEquals(before, storage.readAddressBook().orElseThrow());
        }
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
        assertEquals(Optional.of(new Subject("Math")), reloadedAt(1).getSubject());
        logic.execute("edit 2 s/");
        assertEquals(new PersonBuilder(student).withPhone("88888888").build(), reloadedAt(1));
    }

    @Test
    public void subjectDoesNotChangeIdentityOrPermitInvalidIndexes() throws Exception {
        logic.execute(ADD + " s/Math");
        assertThrows(CommandException.class, () -> logic.execute(ADD + " s/English"));
        assertThrows(CommandException.class, () -> logic.execute("edit 2 s/English"));
        assertEquals(Optional.of(new Subject("Math")), reloaded().getSubject());
    }

    @Test
    public void descriptorCopy_distinguishesOmittedSetAndCleared() {
        EditPersonDescriptor omitted = new EditPersonDescriptor();
        EditPersonDescriptor clear = new EditPersonDescriptor();
        clear.setSubject(Optional.empty());
        EditPersonDescriptor set = new EditPersonDescriptor();
        set.setSubject(Optional.of(new Subject("Math")));
        assertFalse(omitted.isAnyFieldEdited());
        assertTrue(clear.isAnyFieldEdited());
        assertNotEquals(omitted, clear);
        assertNotEquals(clear, set);
        assertEquals(clear, new EditPersonDescriptor(clear));
        assertEquals(set, new EditPersonDescriptor(set));
    }

    private Person reloaded() throws Exception {
        return reloadedAt(0);
    }

    private Person reloadedAt(int index) throws Exception {
        return storage.readAddressBook().orElseThrow().getPersonList().get(index);
    }
}
