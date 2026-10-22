package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.LessonCommand.Operation;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.School;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.PersonBuilder;

public class LessonCommandTest {
    private static final Subject MATH = new Subject("Math");
    private static final Subject ENGLISH = new Subject("English");
    private static final WeeklyLessonSlot MONDAY = new WeeklyLessonSlot(DayOfWeek.MONDAY,
            LocalTime.of(16, 0), LocalTime.of(17, 30));
    private static final WeeklyLessonSlot TUESDAY = new WeeklyLessonSlot(DayOfWeek.TUESDAY,
            LocalTime.of(16, 0), LocalTime.of(17, 30));
    private static final HourlyRate RATE = new HourlyRate(new BigDecimal("45.50"));
    private static final LessonLocation LIBRARY = new LessonLocation("Library");
    private static final Index INDEX_FIRST_LESSON = Index.fromOneBased(1);
    private static final Index INDEX_SECOND_LESSON = Index.fromOneBased(2);

    private static final Lesson FULL_LESSON = new Lesson(MATH, MONDAY, RATE, Optional.of(LIBRARY),
            Optional.of(LessonMode.PHYSICAL));

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private static LessonCommand add(Index student, Subject subject, WeeklyLessonSlot slot) {
        return new LessonCommand(student, Operation.ADD, Optional.empty(), Optional.of(subject),
                Optional.of(slot), Optional.of(RATE), Optional.empty(), Optional.empty());
    }

    private Student withLessons(Student student, Lesson... lessons) {
        return new PersonBuilder(student).withLessons(lessons).build();
    }

    private Student firstStudent() {
        return model.getFilteredPersonList().get(0);
    }

    @Test
    public void constructor_invalidFields_throwsIllegalArgumentException() {
        Optional<Index> lesson = Optional.of(INDEX_FIRST_LESSON);
        Optional<Subject> subject = Optional.of(MATH);
        Optional<WeeklyLessonSlot> slot = Optional.of(MONDAY);
        Optional<HourlyRate> rate = Optional.of(RATE);
        Optional<LessonLocation> location = Optional.of(LIBRARY);
        Optional<LessonMode> mode = Optional.of(LessonMode.VIRTUAL);
        Optional<Index> noIndex = Optional.empty();

        // add with missing field, or with lesson index
        assertThrows(IllegalArgumentException.class, () -> new LessonCommand(INDEX_FIRST_PERSON, Operation.ADD,
                noIndex, subject, slot, Optional.empty(), Optional.empty(), Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> new LessonCommand(INDEX_FIRST_PERSON, Operation.ADD,
                lesson, subject, slot, rate, Optional.empty(), Optional.empty()));
        // edit without index or without any field
        assertThrows(IllegalArgumentException.class, () -> new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                noIndex, subject, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                lesson, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
        // delete without index or with fields
        assertThrows(IllegalArgumentException.class, () -> new LessonCommand(INDEX_FIRST_PERSON, Operation.DELETE,
                noIndex, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> new LessonCommand(INDEX_FIRST_PERSON, Operation.DELETE,
                lesson, subject, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
        // clearing combined with a new value, or on a non-edit operation
        assertThrows(IllegalArgumentException.class, () -> new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                lesson, Optional.empty(), Optional.empty(), Optional.empty(), location, Optional.empty(),
                true, false));
        assertThrows(IllegalArgumentException.class, () -> new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                lesson, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), mode,
                false, true));
        assertThrows(IllegalArgumentException.class, () -> new LessonCommand(INDEX_FIRST_PERSON, Operation.ADD,
                noIndex, subject, slot, rate, Optional.empty(), Optional.empty(), true, false));
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LessonCommand(null, Operation.ADD, Optional.empty(),
                Optional.of(MATH), Optional.of(MONDAY), Optional.of(RATE), Optional.empty(), Optional.empty()));
        assertThrows(NullPointerException.class, () -> new LessonCommand(INDEX_FIRST_PERSON, null,
                Optional.empty(), Optional.of(MATH), Optional.of(MONDAY), Optional.of(RATE), Optional.empty(),
                Optional.empty()));
        assertThrows(NullPointerException.class, () -> add(INDEX_FIRST_PERSON, MATH, MONDAY).execute(null));
    }

    @Test
    public void execute_lessonChanges_preserveSchool() {
        Student student = new PersonBuilder(firstStudent()).withSchool(new School("Nanyang Primary School")).build();
        model.setPerson(firstStudent(), student);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Student withLesson = withLessons(student, new Lesson(MATH, MONDAY, RATE));
        expectedModel.setPerson(student, withLesson);
        assertCommandSuccess(add(INDEX_FIRST_PERSON, MATH, MONDAY), model,
                "Lesson added for " + student.getName() + ".", expectedModel);

        Student withEditedLesson = withLessons(student, new Lesson(ENGLISH, MONDAY, RATE));
        expectedModel.setPerson(withLesson, withEditedLesson);
        LessonCommand edit = new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.of(INDEX_FIRST_LESSON), Optional.of(ENGLISH), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
        assertCommandSuccess(edit, model, "Lesson updated for " + student.getName() + ".", expectedModel);

        expectedModel.setPerson(withEditedLesson, student);
        LessonCommand delete = new LessonCommand(INDEX_FIRST_PERSON, Operation.DELETE,
                Optional.of(INDEX_FIRST_LESSON), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
        assertCommandSuccess(delete, model, "Lesson removed for " + student.getName() + ".", expectedModel);
    }

    @Test
    public void execute_addLesson_success() {
        Student student = firstStudent();
        LessonCommand command = new LessonCommand(INDEX_FIRST_PERSON, Operation.ADD, Optional.empty(),
                Optional.of(MATH), Optional.of(MONDAY), Optional.of(RATE), Optional.of(LIBRARY),
                Optional.of(LessonMode.PHYSICAL));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, withLessons(student, FULL_LESSON));

        assertCommandSuccess(command, model, "Lesson added for " + student.getName() + ".", expectedModel);
    }

    @Test
    public void execute_editLesson_keepsUnspecifiedFields() {
        Student student = withLessons(firstStudent(), FULL_LESSON);
        model.setPerson(firstStudent(), student);
        LessonCommand command = new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.of(INDEX_FIRST_LESSON), Optional.of(ENGLISH), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, withLessons(student,
                new Lesson(ENGLISH, MONDAY, RATE, Optional.of(LIBRARY), Optional.of(LessonMode.PHYSICAL))));

        assertCommandSuccess(command, model, "Lesson updated for " + student.getName() + ".", expectedModel);
    }

    @Test
    public void execute_editArchivedStudentLesson_preservesArchiveStatus() throws CommandException {
        Student archived = withLessons(firstStudent(), FULL_LESSON).withArchived(true);
        model.setPerson(firstStudent(), archived);
        model.updateFilteredPersonList(archived::isSamePerson);
        LessonCommand command = new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.of(INDEX_FIRST_LESSON), Optional.of(ENGLISH), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());

        command.execute(model);

        Student edited = firstStudent();
        assertEquals(withLessons(archived,
                new Lesson(ENGLISH, MONDAY, RATE, Optional.of(LIBRARY), Optional.of(LessonMode.PHYSICAL))), edited);
        assertTrue(edited.isArchived());
    }

    @Test
    public void execute_editLessonAllFields_success() {
        Student student = withLessons(firstStudent(), FULL_LESSON);
        model.setPerson(firstStudent(), student);
        HourlyRate newRate = new HourlyRate(new BigDecimal("60"));
        LessonLocation newLocation = new LessonLocation("Home");
        LessonCommand command = new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.of(INDEX_FIRST_LESSON), Optional.of(ENGLISH), Optional.of(TUESDAY),
                Optional.of(newRate), Optional.of(newLocation), Optional.of(LessonMode.VIRTUAL));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, withLessons(student, new Lesson(ENGLISH, TUESDAY, newRate,
                Optional.of(newLocation), Optional.of(LessonMode.VIRTUAL))));

        assertCommandSuccess(command, model, "Lesson updated for " + student.getName() + ".", expectedModel);
    }

    @Test
    public void execute_editLessonClearOptionalFields_success() {
        Student student = withLessons(firstStudent(), FULL_LESSON);
        model.setPerson(firstStudent(), student);
        LessonCommand command = new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.of(INDEX_FIRST_LESSON), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), true, true);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, withLessons(student, new Lesson(MATH, MONDAY, RATE)));

        assertCommandSuccess(command, model, "Lesson updated for " + student.getName() + ".", expectedModel);
    }

    @Test
    public void execute_deleteLesson_success() {
        Student student = withLessons(firstStudent(), FULL_LESSON);
        model.setPerson(firstStudent(), student);
        LessonCommand command = new LessonCommand(INDEX_FIRST_PERSON, Operation.DELETE,
                Optional.of(INDEX_FIRST_LESSON), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, withLessons(student));

        assertCommandSuccess(command, model, "Lesson removed for " + student.getName() + ".", expectedModel);
    }

    @Test
    public void execute_invalidStudentIndex_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(add(outOfBounds, MATH, MONDAY), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidLessonIndex_throwsCommandException() {
        Student student = withLessons(firstStudent(), FULL_LESSON);
        model.setPerson(firstStudent(), student);
        LessonCommand edit = new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.of(INDEX_SECOND_LESSON), Optional.of(ENGLISH), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
        LessonCommand delete = new LessonCommand(INDEX_FIRST_PERSON, Operation.DELETE,
                Optional.of(INDEX_SECOND_LESSON), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());

        assertCommandFailure(edit, model, LessonCommand.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
        assertCommandFailure(delete, model, LessonCommand.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_duplicateLesson_throwsCommandException() {
        Student student = withLessons(firstStudent(), new Lesson(MATH, MONDAY, RATE),
                new Lesson(ENGLISH, MONDAY, RATE));
        model.setPerson(firstStudent(), student);

        // add an identical lesson
        assertCommandFailure(add(INDEX_FIRST_PERSON, MATH, MONDAY), model, LessonCommand.MESSAGE_DUPLICATE_LESSON);

        // edit the second lesson to match the first
        LessonCommand edit = new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.of(INDEX_SECOND_LESSON), Optional.of(MATH), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty());
        assertCommandFailure(edit, model, LessonCommand.MESSAGE_DUPLICATE_LESSON);
    }

    @Test
    public void equals() {
        LessonCommand command = add(INDEX_FIRST_PERSON, MATH, MONDAY);
        LessonCommand editCommand = new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.of(INDEX_FIRST_LESSON), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), true, false);

        assertTrue(command.equals(command));
        assertTrue(command.equals(add(INDEX_FIRST_PERSON, MATH, MONDAY)));
        assertEquals(command.hashCode(), add(INDEX_FIRST_PERSON, MATH, MONDAY).hashCode());

        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertNotEquals(command, add(INDEX_SECOND_PERSON, MATH, MONDAY));
        assertNotEquals(command, add(INDEX_FIRST_PERSON, ENGLISH, MONDAY));
        assertNotEquals(command, add(INDEX_FIRST_PERSON, MATH, TUESDAY));
        assertNotEquals(command, editCommand);
        assertNotEquals(command, new LessonCommand(INDEX_FIRST_PERSON, Operation.ADD, Optional.empty(),
                Optional.of(MATH), Optional.of(MONDAY), Optional.of(new HourlyRate(new BigDecimal("10"))),
                Optional.empty(), Optional.empty()));
        assertNotEquals(command, new LessonCommand(INDEX_FIRST_PERSON, Operation.ADD, Optional.empty(),
                Optional.of(MATH), Optional.of(MONDAY), Optional.of(RATE), Optional.of(LIBRARY),
                Optional.empty()));
        assertNotEquals(command, new LessonCommand(INDEX_FIRST_PERSON, Operation.ADD, Optional.empty(),
                Optional.of(MATH), Optional.of(MONDAY), Optional.of(RATE), Optional.empty(),
                Optional.of(LessonMode.VIRTUAL)));

        // differing lesson index, and differing clear flags
        LessonCommand editSecond = new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.of(INDEX_SECOND_LESSON), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), true, false);
        LessonCommand editClearMode = new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.of(INDEX_FIRST_LESSON), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), false, true);
        assertNotEquals(editCommand, editSecond);
        assertNotEquals(editCommand, editClearMode);
        assertEquals(List.of(editCommand).get(0), editCommand);
    }
}
