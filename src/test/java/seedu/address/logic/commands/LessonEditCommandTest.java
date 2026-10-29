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
import seedu.address.logic.commands.LessonEditCommand.EditLessonDescriptor;
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

public class LessonEditCommandTest {
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

    private Student withLessons(Student student, Lesson... lessons) {
        return new PersonBuilder(student).withLessons(lessons).build();
    }

    private Student firstStudent() {
        return model.getFilteredPersonList().get(0);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LessonEditCommand(null, INDEX_FIRST_LESSON,
                new EditLessonDescriptorBuilder().withSubject(MATH).build()));
        assertThrows(NullPointerException.class, () -> new LessonEditCommand(INDEX_FIRST_PERSON, null,
                new EditLessonDescriptorBuilder().withSubject(MATH).build()));
        assertThrows(NullPointerException.class, () -> new LessonEditCommand(INDEX_FIRST_PERSON,
                INDEX_FIRST_LESSON, null));
    }

    @Test
    public void constructor_emptyEdit_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new LessonEditCommand(INDEX_FIRST_PERSON,
                INDEX_FIRST_LESSON, new EditLessonDescriptorBuilder().build()));
    }

    @Test
    public void execute_editLesson_keepsUnspecifiedFields() {
        Student student = withLessons(firstStudent(), FULL_LESSON);
        model.setPerson(firstStudent(), student);
        Command command = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON,
                new EditLessonDescriptorBuilder()
                        .withSubject(ENGLISH).build());

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, withLessons(student,
                new Lesson(ENGLISH, MONDAY, RATE, Optional.of(LIBRARY), Optional.of(LessonMode.PHYSICAL))));

        assertCommandSuccess(command, model, "Lesson updated for " + student.getName() + ".", expectedModel);
    }

    @Test
    public void execute_editArchivedStudentLesson_preservesArchiveStatus() throws CommandException {
        Student archived = withLessons(firstStudent(), FULL_LESSON).withArchived(true);
        model.setPerson(firstStudent(), archived);
        new ListArchiveCommand().execute(model);
        model.updateFilteredPersonList(archived::isSamePerson);
        Command command = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON,
                new EditLessonDescriptorBuilder()
                        .withSubject(ENGLISH).build());

        command.execute(model);

        Student edited = firstStudent();
        assertEquals(withLessons(archived,
                new Lesson(ENGLISH, MONDAY, RATE, Optional.of(LIBRARY), Optional.of(LessonMode.PHYSICAL))), edited);
        assertTrue(edited.isArchived());
    }

    @Test
    public void execute_editArchivedLessonOverlappingOwnLesson_warns() throws CommandException {
        Student archived = withLessons(firstStudent(), FULL_LESSON, new Lesson(ENGLISH, TUESDAY, RATE))
                .withArchived(true);
        model.setPerson(firstStudent(), archived);
        new ListArchiveCommand().execute(model);
        Command command = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_LESSON,
                new EditLessonDescriptorBuilder()
                        .withLessonSlot(MONDAY).build());

        CommandResult result = command.execute(model);

        assertTrue(result.isWarning());
        assertTrue(result.getFeedbackToUser().contains(archived.getName() + ", lesson 1"));
        assertTrue(result.getFeedbackToUser().contains("overlaps with 1 lesson(s)"));
        assertEquals(withLessons(archived, FULL_LESSON, new Lesson(ENGLISH, MONDAY, RATE)), firstStudent());
    }

    @Test
    public void execute_editLessonAllFields_success() {
        Student student = withLessons(firstStudent(), FULL_LESSON);
        model.setPerson(firstStudent(), student);
        HourlyRate newRate = new HourlyRate(new BigDecimal("60"));
        LessonLocation newLocation = new LessonLocation("Home");
        Command command = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON,
                new EditLessonDescriptorBuilder()
                        .withSubject(ENGLISH)
                        .withLessonSlot(TUESDAY)
                        .withHourlyRate(newRate)
                        .withLessonLocation(Optional.of(newLocation))
                        .withLessonMode(Optional.of(LessonMode.VIRTUAL)).build());

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, withLessons(student, new Lesson(ENGLISH, TUESDAY, newRate,
                Optional.of(newLocation), Optional.of(LessonMode.VIRTUAL))));

        assertCommandSuccess(command, model, "Lesson updated for " + student.getName() + ".", expectedModel);
    }

    @Test
    public void execute_editLessonClearOptionalFields_success() {
        Student student = withLessons(firstStudent(), FULL_LESSON);
        model.setPerson(firstStudent(), student);
        Command command = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON,
                new EditLessonDescriptorBuilder()
                        .withLessonLocation(Optional.empty())
                        .withLessonMode(Optional.empty()).build());

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, withLessons(student, new Lesson(MATH, MONDAY, RATE)));

        assertCommandSuccess(command, model, "Lesson updated for " + student.getName() + ".", expectedModel);
    }

    @Test
    public void execute_editNonLastLesson_warnsForChangedLesson() throws CommandException {
        Student student = withLessons(firstStudent(), FULL_LESSON, new Lesson(ENGLISH, TUESDAY, RATE));
        model.setPerson(firstStudent(), student);
        model.addPerson(new PersonBuilder().withName("Hidden Peer").withLessons(FULL_LESSON).build());
        model.updateFilteredPersonList(student::isSamePerson);
        HourlyRate newRate = new HourlyRate(new BigDecimal("60"));

        CommandResult result = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON,
                new EditLessonDescriptorBuilder().withHourlyRate(newRate).build()).execute(model);

        assertTrue(result.isWarning());
        assertTrue(result.getFeedbackToUser().startsWith(LessonClashes.MESSAGE_CLASH_WARNING));
        assertTrue(result.getFeedbackToUser().contains("Warning: Lesson 1 for " + student.getName()));
        assertTrue(result.getFeedbackToUser().contains("Hidden Peer, lesson 1"));
        assertEquals(new Lesson(MATH, MONDAY, newRate, FULL_LESSON.getLocation(), FULL_LESSON.getMode()),
                firstStudent().getLessons().getFirst());
    }

    @Test
    public void execute_editNonLastLesson_ignoresClashesOfUnchangedLesson() throws CommandException {
        Lesson otherLesson = new Lesson(ENGLISH, TUESDAY, RATE);
        Student student = withLessons(firstStudent(), FULL_LESSON, otherLesson);
        model.setPerson(firstStudent(), student);
        model.addPerson(new PersonBuilder().withName("Hidden Peer").withLessons(otherLesson).build());
        model.updateFilteredPersonList(student::isSamePerson);

        CommandResult result = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON,
                new EditLessonDescriptorBuilder().withHourlyRate(new HourlyRate(new BigDecimal("60")))
                        .build()).execute(model);

        assertFalse(result.isWarning());
        assertEquals("Lesson updated for " + student.getName() + ".", result.getFeedbackToUser());
    }

    @Test
    public void constructor_editDescriptor_isDefensivelyCopied() {
        Student student = withLessons(firstStudent(), FULL_LESSON);
        model.setPerson(firstStudent(), student);
        EditLessonDescriptor descriptor = new EditLessonDescriptorBuilder().withSubject(ENGLISH).build();
        LessonEditCommand command = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON, descriptor);
        descriptor.setSubject(MATH);
        descriptor.setLessonLocation(Optional.empty());

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, withLessons(student,
                new Lesson(ENGLISH, MONDAY, RATE, FULL_LESSON.getLocation(), FULL_LESSON.getMode())));
        assertCommandSuccess(command, model, "Lesson updated for " + student.getName() + ".", expectedModel);
    }

    @Test
    public void execute_preservesSchool() {
        Student student = new PersonBuilder(firstStudent()).withSchool(new School("Nanyang Primary School"))
                .build();
        student = withLessons(student, new Lesson(MATH, MONDAY, RATE));
        model.setPerson(firstStudent(), student);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(student, withLessons(student, new Lesson(ENGLISH, MONDAY, RATE)));
        LessonEditCommand command = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON,
                new EditLessonDescriptorBuilder().withSubject(ENGLISH).build());
        assertCommandSuccess(command, model, "Lesson updated for " + student.getName() + ".", expectedModel);
    }

    @Test
    public void execute_invalidStudentIndex_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new LessonEditCommand(outOfBounds, INDEX_FIRST_LESSON,
                new EditLessonDescriptorBuilder().withSubject(MATH).build()), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidLessonIndex_throwsCommandException() {
        Student student = withLessons(firstStudent(), FULL_LESSON);
        model.setPerson(firstStudent(), student);
        LessonEditCommand command = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_LESSON,
                new EditLessonDescriptorBuilder().withSubject(ENGLISH).build());
        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_LESSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_duplicateLesson_throwsCommandException() {
        Student student = withLessons(firstStudent(), new Lesson(MATH, MONDAY, RATE),
                new Lesson(ENGLISH, MONDAY, RATE));
        model.setPerson(firstStudent(), student);

        Command edit = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_LESSON,
                new EditLessonDescriptorBuilder()
                        .withSubject(MATH).build());
        assertCommandFailure(edit, model, LessonAddCommand.MESSAGE_DUPLICATE_LESSON);
    }

    @Test
    public void execute_filteredStudentList_usesDisplayedIndex() throws CommandException {
        Student hidden = firstStudent();
        Student target = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Student withLesson = withLessons(target, new Lesson(MATH, MONDAY, RATE));
        model.setPerson(target, withLesson);
        model.updateFilteredPersonList(target::isSamePerson);

        new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON,
                new EditLessonDescriptorBuilder().withSubject(ENGLISH).build()).execute(model);
        assertEquals(withLessons(target, new Lesson(ENGLISH, MONDAY, RATE)), firstStudent());
        assertEquals(hidden, model.getAddressBook().getPersonList().getFirst());
    }

    @Test
    public void equals() {
        EditLessonDescriptor descriptor = new EditLessonDescriptorBuilder().withSubject(MATH).build();
        LessonEditCommand edit = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON, descriptor);
        LessonEditCommand sameEdit = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON,
                new EditLessonDescriptor(descriptor));
        assertEquals(edit, sameEdit);
        assertEquals(edit.hashCode(), sameEdit.hashCode());
        assertNotEquals(edit, new LessonEditCommand(INDEX_SECOND_PERSON, INDEX_FIRST_LESSON, descriptor));
        assertNotEquals(edit, new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_LESSON, descriptor));
        assertNotEquals(edit, new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON,
                new EditLessonDescriptorBuilder().withSubject(ENGLISH).build()));
        assertNotEquals(edit, null);

        EditLessonDescriptor clearLocation = new EditLessonDescriptor(descriptor);
        clearLocation.setLessonLocation(Optional.empty());
        assertNotEquals(descriptor, clearLocation);
        EditLessonDescriptor clearMode = new EditLessonDescriptor(descriptor);
        clearMode.setLessonMode(Optional.empty());
        assertNotEquals(descriptor, clearMode);
    }

    @Test
    public void equals_differentClearFields_returnsFalse() {
        LessonEditCommand editCommand = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON,
                new EditLessonDescriptorBuilder().withLessonLocation(Optional.empty()).build());

        // differing lesson index, and differing clear flags
        Command editSecond = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_LESSON,
                new EditLessonDescriptorBuilder()
                        .withLessonLocation(Optional.empty()).build());
        Command editClearMode = new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_FIRST_LESSON,
                new EditLessonDescriptorBuilder()
                        .withLessonMode(Optional.empty()).build());
        assertNotEquals(editCommand, editSecond);
        assertNotEquals(editCommand, editClearMode);
    }
}
