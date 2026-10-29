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
import seedu.address.testutil.EditLessonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

public class LessonAddCommandTest {
    private static final Subject MATH = new Subject("Math");
    private static final Subject ENGLISH = new Subject("English");
    private static final WeeklyLessonSlot MONDAY = new WeeklyLessonSlot(DayOfWeek.MONDAY,
            LocalTime.of(16, 0), LocalTime.of(17, 30));
    private static final WeeklyLessonSlot TUESDAY = new WeeklyLessonSlot(DayOfWeek.TUESDAY,
            LocalTime.of(16, 0), LocalTime.of(17, 30));
    private static final HourlyRate RATE = new HourlyRate(new BigDecimal("45.50"));
    private static final LessonLocation LIBRARY = new LessonLocation("Library");
    private static final Index INDEX_FIRST_LESSON = Index.fromOneBased(1);

    private static final Lesson FULL_LESSON = new Lesson(MATH, MONDAY, RATE, Optional.of(LIBRARY),
            Optional.of(LessonMode.PHYSICAL));

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private static LessonAddCommand add(Index student, Subject subject, WeeklyLessonSlot slot) {
        return new LessonAddCommand(student,
                new Lesson(subject, slot, RATE,
                        Optional.empty(), Optional.empty()));
    }

    private Student withLessons(Student student, Lesson... lessons) {
        return new PersonBuilder(student).withLessons(lessons).build();
    }

    private Student firstStudent() {
        return model.getFilteredPersonList().get(0);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LessonAddCommand(null, FULL_LESSON));
        assertThrows(NullPointerException.class, () -> new LessonAddCommand(INDEX_FIRST_PERSON, null));
        assertThrows(NullPointerException.class, () -> add(INDEX_FIRST_PERSON, MATH, MONDAY).execute(null));
    }

    @Test
    public void execute_addLesson_success() {
        Student student = firstStudent();
        Command command = new LessonAddCommand(INDEX_FIRST_PERSON,
                new Lesson(MATH, MONDAY, RATE,
                        Optional.of(LIBRARY), Optional.of(LessonMode.PHYSICAL)));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, withLessons(student, FULL_LESSON));

        assertCommandSuccess(command, model,
                "Added lesson for " + student.getName() + ": " + FULL_LESSON, expectedModel);
    }

    @Test
    public void execute_addArchivedLessonOverlappingOwnLesson_warns() throws CommandException {
        Student archived = withLessons(firstStudent(), FULL_LESSON).withArchived(true);
        model.setPerson(firstStudent(), archived);
        new ListArchiveCommand().execute(model);

        CommandResult result = add(INDEX_FIRST_PERSON, ENGLISH, MONDAY).execute(model);

        assertTrue(result.isWarning());
        assertTrue(result.getFeedbackToUser().contains(archived.getName() + ", lesson 1"));
        assertTrue(result.getFeedbackToUser().contains("overlaps with 1 lesson(s)"));
        assertEquals(withLessons(archived, FULL_LESSON, new Lesson(ENGLISH, MONDAY, RATE)), firstStudent());
    }

    @Test
    public void execute_addArchivedLesson_warnsForHiddenArchivedPeerOnly() throws CommandException {
        Student archived = firstStudent().withArchived(true);
        model.setPerson(firstStudent(), archived);
        Student archivedPeer = new PersonBuilder().withName("Archived Peer").withLessons(FULL_LESSON).build()
                .withArchived(true);
        Student activePeer = new PersonBuilder().withName("Active Peer").withLessons(FULL_LESSON).build();
        model.addPerson(archivedPeer);
        model.addPerson(activePeer);
        new ListArchiveCommand().execute(model);
        model.updateFilteredPersonList(archived::isSamePerson);

        CommandResult result = add(INDEX_FIRST_PERSON, ENGLISH, MONDAY).execute(model);

        assertTrue(result.isWarning());
        assertTrue(result.getFeedbackToUser().contains("Archived Peer, lesson 1"));
        assertFalse(result.getFeedbackToUser().contains("Active Peer"));
        assertTrue(result.getFeedbackToUser().contains("overlaps with 1 lesson(s)"));
    }

    @Test
    public void execute_addActiveLesson_warnsForHiddenActivePeerOnly() throws CommandException {
        Student student = firstStudent();
        Student activePeer = new PersonBuilder().withName("Active Peer").withLessons(FULL_LESSON).build();
        Student archivedPeer = new PersonBuilder().withName("Archived Peer").withLessons(FULL_LESSON).build()
                .withArchived(true);
        model.addPerson(activePeer);
        model.addPerson(archivedPeer);
        model.updateFilteredPersonList(student::isSamePerson);

        CommandResult result = add(INDEX_FIRST_PERSON, ENGLISH, MONDAY).execute(model);

        assertTrue(result.isWarning());
        assertTrue(result.getFeedbackToUser().contains("Active Peer, lesson 1"));
        assertFalse(result.getFeedbackToUser().contains("Archived Peer"));
        assertTrue(result.getFeedbackToUser().contains("overlaps with 1 lesson(s)"));
    }

    @Test
    public void execute_preservesSchool() {
        Student student = new PersonBuilder(firstStudent()).withSchool(new School("Nanyang Primary School"))
                .build();
        model.setPerson(firstStudent(), student);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, withLessons(student, new Lesson(MATH, MONDAY, RATE)));
        assertCommandSuccess(add(INDEX_FIRST_PERSON, MATH, MONDAY), model,
                "Added lesson for " + student.getName() + ": " + new Lesson(MATH, MONDAY, RATE), expectedModel);
    }

    @Test
    public void execute_invalidStudentIndex_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(add(outOfBounds, MATH, MONDAY), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_duplicateLesson_throwsCommandException() {
        Student student = withLessons(firstStudent(), new Lesson(MATH, MONDAY, RATE),
                new Lesson(ENGLISH, MONDAY, RATE));
        model.setPerson(firstStudent(), student);

        assertCommandFailure(add(INDEX_FIRST_PERSON, MATH, MONDAY), model, LessonAddCommand.MESSAGE_DUPLICATE_LESSON);
    }

    @Test
    public void execute_filteredStudentList_usesDisplayedIndex() throws CommandException {
        Student hidden = firstStudent();
        Student target = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        model.updateFilteredPersonList(target::isSamePerson);

        add(INDEX_FIRST_PERSON, MATH, MONDAY).execute(model);
        assertEquals(withLessons(target, new Lesson(MATH, MONDAY, RATE)), firstStudent());
        assertEquals(hidden, model.getAddressBook().getPersonList().getFirst());
    }

    @Test
    public void equals() {
        Command command = add(INDEX_FIRST_PERSON, MATH, MONDAY);
        Command editCommand = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON,
                new EditLessonDescriptorBuilder()
                        .withLessonLocation(Optional.empty()).build());

        assertTrue(command.equals(command));
        assertTrue(command.equals(add(INDEX_FIRST_PERSON, MATH, MONDAY)));
        assertEquals(command.hashCode(), add(INDEX_FIRST_PERSON, MATH, MONDAY).hashCode());

        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertNotEquals(command, add(INDEX_SECOND_PERSON, MATH, MONDAY));
        assertNotEquals(command, add(INDEX_FIRST_PERSON, ENGLISH, MONDAY));
        assertNotEquals(command, add(INDEX_FIRST_PERSON, MATH, TUESDAY));
        assertNotEquals(command, editCommand);
        assertNotEquals(command, new LessonAddCommand(INDEX_FIRST_PERSON,
                new Lesson(MATH, MONDAY, new HourlyRate(new BigDecimal("10")),
                        Optional.empty(), Optional.empty())));
        assertNotEquals(command, new LessonAddCommand(INDEX_FIRST_PERSON,
                new Lesson(MATH, MONDAY, RATE,
                        Optional.of(LIBRARY), Optional.empty())));
        assertNotEquals(command, new LessonAddCommand(INDEX_FIRST_PERSON,
                new Lesson(MATH, MONDAY, RATE,
                        Optional.empty(), Optional.of(LessonMode.VIRTUAL))));
    }
}
