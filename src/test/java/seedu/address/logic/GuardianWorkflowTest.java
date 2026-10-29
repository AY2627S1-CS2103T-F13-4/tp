package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
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
import seedu.address.model.person.Guardian;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Covers guardian CRUD through command parsing, execution and automatic persistence. */
public class GuardianWorkflowTest {
    private static final Guardian CONTACT = new Guardian(new Name("Janet Tan"), new Phone("91234567"));

    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;
    private Student original;

    @BeforeEach
    public void setUp() throws IOException {
        original = new PersonBuilder().withSchoolingLevel(new SchoolingLevel("Primary 5"))
                .withLessons(new Lesson(new Subject("Math"),
                        new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(16, 0), LocalTime.of(17, 0)),
                        new HourlyRate(new BigDecimal("45.50"))))
                .build();
        AddressBook book = new AddressBook();
        book.addPerson(original);
        model = new ModelManager(book, new UserPrefs());
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("students.json"));
        storage.saveAddressBook(model.getAddressBook());
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private void assertStudent(Student expected) throws Exception {
        assertEquals(expected, model.getAddressBook().getPersonList().get(0));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_crud_preservesOtherFieldsAndSavesEveryChange() throws Exception {
        logic.execute("guardian-add 1 name/Janet Tan phone/91234567");
        Student created = new PersonBuilder(original).withGuardian(CONTACT).build();
        assertStudent(created);
        logic.execute("list");
        assertTrue(Messages.format(created).contains("Guardian: Janet Tan: 91234567"));

        logic.execute("guardian-edit 1 phone/87654321");
        Guardian changedPhone = new Guardian(CONTACT.getName(), new Phone("87654321"));
        assertStudent(new PersonBuilder(original).withGuardian(changedPhone).build());

        logic.execute("guardian-edit 1 name/John Tan");
        Guardian changedName = new Guardian(new Name("John Tan"), changedPhone.getPhone());
        assertStudent(new PersonBuilder(original).withGuardian(changedName).build());

        logic.execute("guardian-edit 1 name/Janet Tan phone/91234567");
        assertStudent(created);

        logic.execute("guardian-delete 1");
        assertStudent(original);
    }

    @Test
    public void execute_existingOrMissingGuardian_rejectsWithoutChangingMemoryOrFile() throws Exception {
        logic.execute("list");
        for (String command : new String[]{"guardian-edit 1 name/Janet", "guardian-delete 1"}) {
            assertThrows(CommandException.class, () -> logic.execute(command));
            assertStudent(original);
        }
        logic.execute("guardian-add 1 name/Janet Tan phone/91234567");
        assertThrows(CommandException.class, () -> logic.execute("guardian-add 1 name/John phone/87654321"));
        assertStudent(new PersonBuilder(original).withGuardian(CONTACT).build());
    }

    @Test
    public void execute_filteredList_changesOnlyDisplayedStudentAndKeepsFilter() throws Exception {
        Student second = new PersonBuilder(original).withName("Bob Tan").build();
        model.addPerson(second);
        logic.execute("find Bob");
        logic.execute("guardian-add 1 name/Janet Tan phone/91234567");
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(original, model.getAddressBook().getPersonList().get(0));
        assertEquals(new PersonBuilder(second).withGuardian(CONTACT).build(),
                model.getFilteredPersonList().get(0));
        assertThrows(CommandException.class, () -> logic.execute("guardian-delete 2"));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_invalidInput_preservesMemoryAndSavedData() throws Exception {
        logic.execute("guardian-add 1 name/Janet Tan phone/91234567");
        Student expected = new PersonBuilder(original).withGuardian(CONTACT).build();
        for (String command : new String[]{
            "guardian-add", "guardian-add 0 name/Janet phone/91234567", "guardian-add x name/Janet phone/91234567",
            "guardian-add 1 name/Janet", "guardian-add 1 phone/91234567", "guardian-add 1 name/ phone/91234567",
            "guardian-add 1 name/Janet phone/abc", "guardian-add 1 name/Janet name/John phone/91234567",
            "guardian-add 1 name/Janet phone/91234567 phone/87654321", "guardian-edit 1", "guardian-edit 1 name/",
            "guardian-edit 1 phone/", "guardian-edit 1 phone/12", "guardian-edit 1 name/Janet Tan phone/9123+4567",
            "guardian-edit 1 subject/Math", "guardian-add 1 name/Janet phone/91234567 rate/50",
            "guardian-edit 1 name/John name/Janet", "guardian-edit 1 phone/91234567 phone/87654321",
            "guardian-delete 1 name/Janet", "guardian-delete 0", "guardian-delete 2147483648"
        }) {
            assertThrows(ParseException.class, () -> logic.execute(command), command);
            assertStudent(expected);
        }
        assertThrows(CommandException.class, () -> logic.execute("guardian-edit 2 name/John"));
        assertStudent(expected);
    }

    @Test
    public void execute_studentEdits_preserveGuardian() throws Exception {
        logic.execute("guardian-add 1 name/Janet Tan phone/91234567");
        logic.execute("edit 1 phone/90000000 level/Primary 6");
        assertStudent(new PersonBuilder(original).withGuardian(CONTACT).withPhone("90000000")
                .withSchoolingLevel(new SchoolingLevel("Primary 6")).build());
    }

    @Test
    public void execute_siblings_allowIndependentCopiesOfSameGuardian() throws Exception {
        model.addPerson(new PersonBuilder(original).withName("Bob Tan").build());
        logic.execute("guardian-add 1 name/Janet Tan phone/91234567");
        logic.execute("guardian-add 2 name/Janet Tan phone/91234567");
        logic.execute("guardian-delete 1");
        assertTrue(model.getFilteredPersonList().get(0).getGuardian().isEmpty());
        assertEquals(CONTACT, model.getFilteredPersonList().get(1).getGuardian().orElseThrow());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }
}
