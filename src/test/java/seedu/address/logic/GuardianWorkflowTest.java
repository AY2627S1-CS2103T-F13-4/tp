package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
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

/** Covers guardian CRUD through command parsing, execution and automatic persistence. */
public class GuardianWorkflowTest {
    private static final GuardianContact CONTACT = new GuardianContact(new Name("Janet Tan"), new Phone("91234567"));

    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;
    private Person original;

    @BeforeEach
    public void setUp() {
        original = new PersonBuilder().withSubjects(new Subject("Math"), new Subject("English"))
                .withSchoolingLevel(new SchoolingLevel("Primary 5"))
                .withWeeklyLessonSlot(new WeeklyLessonSlot(DayOfWeek.MONDAY,
                        LocalTime.of(16, 0), LocalTime.of(17, 0)))
                .withHourlyRate(new HourlyRate(new BigDecimal("45.50"))).build();
        AddressBook book = new AddressBook();
        book.addPerson(original);
        model = new ModelManager(book, new UserPrefs());
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("students.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private void assertStudent(Person expected) throws Exception {
        assertEquals(expected, model.getAddressBook().getPersonList().get(0));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_crud_preservesOtherFieldsAndSavesEveryChange() throws Exception {
        logic.execute("guardian-add 1 n/Janet Tan p/91234567");
        Person created = new PersonBuilder(original).withGuardianContact(CONTACT).build();
        assertStudent(created);
        logic.execute("list");
        assertTrue(Messages.format(created).contains("Guardian: Janet Tan: 91234567"));

        logic.execute("guardian-edit 1 p/87654321");
        GuardianContact changedPhone = new GuardianContact(CONTACT.getName(), new Phone("87654321"));
        assertStudent(new PersonBuilder(original).withGuardianContact(changedPhone).build());

        logic.execute("guardian-edit 1 n/John Tan");
        GuardianContact changedName = new GuardianContact(new Name("John Tan"), changedPhone.getPhone());
        assertStudent(new PersonBuilder(original).withGuardianContact(changedName).build());

        logic.execute("guardian-edit 1 n/Janet Tan p/91234567");
        assertStudent(created);

        logic.execute("guardian-delete 1");
        assertStudent(original);
    }

    @Test
    public void execute_existingOrMissingGuardian_rejectsWithoutChangingMemoryOrFile() throws Exception {
        logic.execute("list");
        for (String command : new String[]{"guardian-edit 1 n/Janet", "guardian-delete 1"}) {
            assertThrows(CommandException.class, () -> logic.execute(command));
            assertStudent(original);
        }
        logic.execute("guardian-add 1 n/Janet Tan p/91234567");
        assertThrows(CommandException.class, () -> logic.execute("guardian-add 1 n/John p/87654321"));
        assertStudent(new PersonBuilder(original).withGuardianContact(CONTACT).build());
    }

    @Test
    public void execute_filteredList_changesOnlyDisplayedStudentAndKeepsFilter() throws Exception {
        Person second = new PersonBuilder(original).withName("Bob Tan").build();
        model.addPerson(second);
        logic.execute("find Bob");
        logic.execute("guardian-add 1 n/Janet Tan p/91234567");
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(original, model.getAddressBook().getPersonList().get(0));
        assertEquals(new PersonBuilder(second).withGuardianContact(CONTACT).build(),
                model.getFilteredPersonList().get(0));
        assertThrows(CommandException.class, () -> logic.execute("guardian-delete 2"));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_invalidInput_preservesMemoryAndSavedData() throws Exception {
        logic.execute("guardian-add 1 n/Janet Tan p/91234567");
        Person expected = new PersonBuilder(original).withGuardianContact(CONTACT).build();
        for (String command : new String[]{
            "guardian-add", "guardian-add 0 n/Janet p/91234567", "guardian-add x n/Janet p/91234567",
            "guardian-add 1 n/Janet", "guardian-add 1 p/91234567", "guardian-add 1 n/ p/91234567",
            "guardian-add 1 n/Janet p/abc", "guardian-add 1 n/Janet n/John p/91234567",
            "guardian-add 1 n/Janet p/91234567 p/87654321", "guardian-edit 1", "guardian-edit 1 n/",
            "guardian-edit 1 p/", "guardian-edit 1 p/12", "guardian-edit 1 n/Janet Tan p/+6591234567",
            "guardian-edit 1 s/Math", "guardian-add 1 n/Janet p/91234567 r/50",
            "guardian-edit 1 n/John n/Janet", "guardian-edit 1 p/91234567 p/87654321",
            "guardian-delete 1 n/Janet", "guardian-delete 0", "guardian-delete 2147483648"
        }) {
            assertThrows(ParseException.class, () -> logic.execute(command), command);
            assertStudent(expected);
        }
        assertThrows(CommandException.class, () -> logic.execute("guardian-edit 2 n/John"));
        assertStudent(expected);
    }

    @Test
    public void execute_studentEdits_preserveGuardian() throws Exception {
        logic.execute("guardian-add 1 n/Janet Tan p/91234567");
        logic.execute("edit 1 r/50 s/Science l/Primary 6 i/Tue 2.00pm 3.00pm");
        assertStudent(new PersonBuilder(original).withGuardianContact(CONTACT)
                .withHourlyRate(new HourlyRate(new BigDecimal("50"))).withSubject(new Subject("Science"))
                .withSchoolingLevel(new SchoolingLevel("Primary 6"))
                .withWeeklyLessonSlot(new WeeklyLessonSlot(DayOfWeek.TUESDAY,
                        LocalTime.of(14, 0), LocalTime.of(15, 0))).build());
    }

    @Test
    public void execute_siblings_allowIndependentCopiesOfSameGuardian() throws Exception {
        model.addPerson(new PersonBuilder(original).withName("Bob Tan").build());
        logic.execute("guardian-add 1 n/Janet Tan p/91234567");
        logic.execute("guardian-add 2 n/Janet Tan p/91234567");
        logic.execute("guardian-delete 1");
        assertTrue(model.getFilteredPersonList().get(0).getGuardianContact().isEmpty());
        assertEquals(CONTACT, model.getFilteredPersonList().get(1).getGuardianContact().orElseThrow());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }
}
