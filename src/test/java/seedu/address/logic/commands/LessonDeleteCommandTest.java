package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
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

public class LessonDeleteCommandTest {
    private static final Subject MATH = new Subject("Math");
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

    private Student withLessons(Student student, Lesson... lessons) {
        return new PersonBuilder(student).withLessons(lessons).build();
    }

    private Student firstStudent() {
        return model.getFilteredPersonList().get(0);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LessonDeleteCommand(null, INDEX_FIRST_LESSON));
        assertThrows(NullPointerException.class, () -> new LessonDeleteCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_deleteLesson_success() {
        Student student = withLessons(firstStudent(), FULL_LESSON);
        model.setPerson(firstStudent(), student);
        Command command = new LessonDeleteCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, withLessons(student));

        assertCommandSuccess(command, model, "Lesson removed for " + student.getName() + ".", expectedModel);
    }

    @Test
    public void execute_preservesSchool() {
        Student student = new PersonBuilder(firstStudent()).withSchool(new School("Nanyang Primary School"))
                .build();
        student = withLessons(student, new Lesson(MATH, MONDAY, RATE));
        model.setPerson(firstStudent(), student);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, withLessons(student));
        LessonDeleteCommand command = new LessonDeleteCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON);
        assertCommandSuccess(command, model, "Lesson removed for " + student.getName() + ".", expectedModel);
    }

    @Test
    public void execute_invalidStudentIndex_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new LessonDeleteCommand(outOfBounds, INDEX_FIRST_LESSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidLessonIndex_throwsCommandException() {
        Student student = withLessons(firstStudent(), FULL_LESSON);
        model.setPerson(firstStudent(), student);
        LessonDeleteCommand command = new LessonDeleteCommand(INDEX_FIRST_PERSON, INDEX_SECOND_LESSON);
        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredStudentList_usesDisplayedIndex() throws CommandException {
        Student hidden = firstStudent();
        Student target = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Student withLesson = withLessons(target, new Lesson(MATH, MONDAY, RATE));
        model.setPerson(target, withLesson);
        model.updateFilteredPersonList(target::isSamePerson);

        new LessonDeleteCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON).execute(model);
        assertEquals(target, firstStudent());
        assertEquals(hidden, model.getAddressBook().getPersonList().getFirst());
    }

    @Test
    public void equals() {
        LessonDeleteCommand delete = new LessonDeleteCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON);
        LessonDeleteCommand sameDelete = new LessonDeleteCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON);
        assertEquals(delete, sameDelete);
        assertEquals(delete.hashCode(), sameDelete.hashCode());
        assertNotEquals(delete, new LessonDeleteCommand(INDEX_SECOND_PERSON, INDEX_FIRST_LESSON));
        assertNotEquals(delete, new LessonDeleteCommand(INDEX_FIRST_PERSON, INDEX_SECOND_LESSON));
        assertNotEquals(delete, new ClearCommand());
        assertNotEquals(delete, null);
    }
}
