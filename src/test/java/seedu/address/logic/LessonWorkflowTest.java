package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.LESSON_SLOT_DESC;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LESSON_SLOT;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** End-to-end command, model and file tests for the lesson commands. */
public class LessonWorkflowTest {
    private static final Lesson MATH = new Lesson(new Subject("Math"), VALID_LESSON_SLOT,
            new HourlyRate(new BigDecimal("45.50")));
    private static final Lesson ENGLISH = new Lesson(new Subject("English"),
            new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(16, 0), LocalTime.of(17, 0)),
            new HourlyRate(new BigDecimal("40")));

    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;
    private Student original;

    @BeforeEach
    public void setUp() {
        original = new PersonBuilder().withSchoolingLevel(new SchoolingLevel("Primary 5")).build();
        AddressBook book = new AddressBook();
        book.addPerson(original);
        model = new ModelManager(book, new UserPrefs());
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("students.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private void assertStudent(Student expected) throws Exception {
        assertEquals(expected, model.getAddressBook().getPersonList().get(0));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_crud_preservesOtherFieldsAndSavesEveryChange() throws Exception {
        logic.execute("lesson-add 1 subject/Math" + LESSON_SLOT_DESC + " rate/45.5");
        assertStudent(new PersonBuilder(original).withLessons(MATH).build());

        logic.execute("lesson-add 1 subject/English slot/Mon 4.00pm 5.00pm rate/40");
        Student both = new PersonBuilder(original).withLessons(MATH, ENGLISH).build();
        assertStudent(both);
        assertTrue(Messages.format(both).contains("Lessons: " + MATH + " | " + ENGLISH));

        logic.execute("lesson-edit 1 2 rate/50");
        Lesson repriced = new Lesson(new Subject("English"), ENGLISH.getLessonSlot(),
                new HourlyRate(new BigDecimal("50")));
        assertStudent(new PersonBuilder(original).withLessons(MATH, repriced).build());

        logic.execute("lesson-edit 1 1 subject/Science");
        Lesson renamed = new Lesson(new Subject("Science"), MATH.getLessonSlot(), MATH.getHourlyRate());
        assertStudent(new PersonBuilder(original).withLessons(renamed, repriced).build());

        logic.execute("lesson-delete 1 1");
        assertStudent(new PersonBuilder(original).withLessons(repriced).build());
        logic.execute("lesson-delete 1 1");
        assertStudent(original);
    }

    @Test
    public void execute_editWithEmptyModeOrLocation_clearsThemAndSaves() throws Exception {
        logic.execute("lesson-add 1 subject/Math" + LESSON_SLOT_DESC + " rate/45.5 mode/virtual location/Zoom");
        Lesson full = new Lesson(MATH.getSubject(), MATH.getLessonSlot(), MATH.getHourlyRate(),
                Optional.of(new LessonLocation("Zoom")), Optional.of(LessonMode.VIRTUAL));
        assertStudent(new PersonBuilder(original).withLessons(full).build());

        logic.execute("lesson-edit 1 1 location/");
        Lesson noLocation = new Lesson(MATH.getSubject(), MATH.getLessonSlot(), MATH.getHourlyRate(),
                Optional.empty(), Optional.of(LessonMode.VIRTUAL));
        assertStudent(new PersonBuilder(original).withLessons(noLocation).build());

        logic.execute("lesson-edit 1 1 mode/ location/Library");
        Lesson swapped = new Lesson(MATH.getSubject(), MATH.getLessonSlot(), MATH.getHourlyRate(),
                Optional.of(new LessonLocation("Library")), Optional.empty());
        assertStudent(new PersonBuilder(original).withLessons(swapped).build());

        assertThrows(ParseException.class, () -> logic.execute("lesson-add 1 subject/Art" + LESSON_SLOT_DESC
                + " rate/45 mode/"));
    }

    @Test
    public void execute_invalidTargets_rejectWithoutChangingMemoryOrFile() throws Exception {
        logic.execute("lesson-add 1 subject/Math" + LESSON_SLOT_DESC + " rate/45.50");
        Student withMath = new PersonBuilder(original).withLessons(MATH).build();
        for (String command : new String[]{"lesson-add 2 subject/Math" + LESSON_SLOT_DESC + " rate/45.50",
            "lesson-edit 2 1 rate/50", "lesson-edit 1 2 rate/50", "lesson-delete 1 2", "lesson-delete 2 1",
            "lesson-add 1 subject/math" + LESSON_SLOT_DESC + " rate/45.5"}) {
            assertThrows(CommandException.class, () -> logic.execute(command));
            assertStudent(withMath);
        }
    }

    @Test
    public void execute_editMakingDuplicate_rejected() throws Exception {
        logic.execute("lesson-add 1 subject/Math" + LESSON_SLOT_DESC + " rate/45.50");
        logic.execute("lesson-add 1 subject/English" + LESSON_SLOT_DESC + " rate/45.50");
        Student expected = new PersonBuilder(original).withLessons(MATH, new Lesson(new Subject("English"),
                VALID_LESSON_SLOT, MATH.getHourlyRate())).build();

        CommandException e = assertThrows(CommandException.class, () -> logic.execute("lesson-edit 1 2 subject/Math"));
        assertEquals(LessonAddCommand.MESSAGE_DUPLICATE_LESSON, e.getMessage());
        assertStudent(expected);
    }

    @Test
    public void execute_invalidInput_rejectWithoutChangingMemoryOrFile() throws Exception {
        logic.execute("lesson-add 1 subject/Math" + LESSON_SLOT_DESC + " rate/45.50");
        Student withMath = new PersonBuilder(original).withLessons(MATH).build();
        for (String command : new String[]{"lesson-add 1 subject/Math",
            "lesson-add 1 subject/123" + LESSON_SLOT_DESC + " rate/45",
            "lesson-add 1 subject/Art slot/Funday 1.00pm 2.00pm rate/45",
            "lesson-add 1 subject/Art" + LESSON_SLOT_DESC + " rate/-1",
            "lesson-edit 1 1", "lesson-edit 1 rate/50", "lesson-edit 1 1 rate/50 rate/60", "lesson-delete 1",
            "lesson-delete 1 1 rate/50", "lesson-add 1 subject/Art" + LESSON_SLOT_DESC + " rate/45 x/y"}) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertStudent(withMath);
        }
    }

    @Test
    public void execute_studentEdits_preserveLessons() throws Exception {
        logic.execute("lesson-add 1 subject/Math" + LESSON_SLOT_DESC + " rate/45.50");
        logic.execute("edit 1 phone/90000000");
        assertStudent(new PersonBuilder(original).withPhone("90000000").withLessons(MATH).build());
    }
}
