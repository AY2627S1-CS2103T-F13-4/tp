package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.NoteEditCommand.EditNoteDescriptor;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.Student;
import seedu.address.testutil.PersonBuilder;

public class NoteEditCommandTest {
    private static final ProgressNote SEPTEMBER = new ProgressNote(LocalDate.of(2026, 9, 1), "Placement test");
    private static final ProgressNote OCTOBER = new ProgressNote(LocalDate.of(2026, 10, 15), "Fractions");
    private static final Index FIRST_NOTE = Index.fromOneBased(1);
    private static final Index SECOND_NOTE = Index.fromOneBased(2);
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private static EditNoteDescriptor descriptor(LocalDate date, String text) {
        EditNoteDescriptor descriptor = new EditNoteDescriptor();
        if (date != null) {
            descriptor.setDate(date);
        }
        if (text != null) {
            descriptor.setText(text);
        }
        return descriptor;
    }

    private Student giveNotes(Student student) {
        Student withNotes = new PersonBuilder(student).withNotes(SEPTEMBER, OCTOBER).build();
        model.setPerson(student, withNotes);
        return withNotes;
    }

    private Student giveFirstStudentNotes() {
        return giveNotes(model.getFilteredPersonList().getFirst());
    }

    @Test
    public void constructor_nullOrEmptyArguments_failure() {
        EditNoteDescriptor descriptor = descriptor(null, "Changed");
        assertThrows(NullPointerException.class, () -> new NoteEditCommand(null, FIRST_NOTE, descriptor));
        assertThrows(NullPointerException.class, () -> new NoteEditCommand(INDEX_FIRST_PERSON, null, descriptor));
        assertThrows(NullPointerException.class, () -> new NoteEditCommand(INDEX_FIRST_PERSON, FIRST_NOTE, null));
        assertThrows(IllegalArgumentException.class, () ->
                new NoteEditCommand(INDEX_FIRST_PERSON, FIRST_NOTE, new EditNoteDescriptor()));
        assertThrows(NullPointerException.class, () ->
                new NoteEditCommand(INDEX_FIRST_PERSON, FIRST_NOTE, descriptor).execute(null));
    }

    @Test
    public void execute_textOnly_changesOnlySelectedNoteAndOpensDetails() {
        Student original = giveFirstStudentNotes();
        ProgressNote edited = new ProgressNote(OCTOBER.getDate(), "Finished fractions");
        Student expectedStudent = original.withNotes(List.of(SEPTEMBER, edited));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, expectedStudent);
        CommandResult expected = new CommandResult(
                String.format(NoteEditCommand.MESSAGE_EDIT_NOTE_SUCCESS, 2, original.getName(), edited),
                INDEX_FIRST_PERSON);

        assertCommandSuccess(new NoteEditCommand(INDEX_FIRST_PERSON, SECOND_NOTE,
                descriptor(null, edited.getText())), model, expected, expectedModel);
    }

    @Test
    public void execute_dateOnly_reordersNotesAndReportsNewNumber() throws Exception {
        Student original = giveFirstStudentNotes();
        ProgressNote edited = new ProgressNote(LocalDate.of(2026, 8, 1), OCTOBER.getText());

        CommandResult result = new NoteEditCommand(INDEX_FIRST_PERSON, SECOND_NOTE,
                descriptor(edited.getDate(), null)).execute(model);

        assertEquals(original.withNotes(List.of(edited, SEPTEMBER)), model.getFilteredPersonList().getFirst());
        assertEquals(String.format(NoteEditCommand.MESSAGE_EDIT_NOTE_SUCCESS, 1, original.getName(), edited),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_bothFields_replacesDateAndText() throws Exception {
        giveFirstStudentNotes();
        ProgressNote edited = new ProgressNote(LocalDate.of(2026, 9, 10), "Revised decimals");

        new NoteEditCommand(INDEX_FIRST_PERSON, FIRST_NOTE,
                descriptor(edited.getDate(), edited.getText())).execute(model);

        assertEquals(List.of(edited, OCTOBER), model.getFilteredPersonList().getFirst().getNotes());
    }

    @Test
    public void execute_sameDate_preservesRelativeOrder() throws Exception {
        giveFirstStudentNotes();
        ProgressNote edited = new ProgressNote(SEPTEMBER.getDate(), OCTOBER.getText());

        new NoteEditCommand(INDEX_FIRST_PERSON, SECOND_NOTE, descriptor(SEPTEMBER.getDate(), null)).execute(model);

        assertEquals(List.of(SEPTEMBER, edited), model.getFilteredPersonList().getFirst().getNotes());
    }

    @Test
    public void execute_unchangedNote_doesNotCountItselfAsDuplicate() throws Exception {
        Student original = giveFirstStudentNotes();

        new NoteEditCommand(INDEX_FIRST_PERSON, FIRST_NOTE,
                descriptor(SEPTEMBER.getDate(), SEPTEMBER.getText())).execute(model);

        assertEquals(original, model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_duplicateDateAndText_failureWithoutChanges() {
        giveFirstStudentNotes();
        assertCommandFailure(new NoteEditCommand(INDEX_FIRST_PERSON, SECOND_NOTE,
                descriptor(SEPTEMBER.getDate(), SEPTEMBER.getText())), model, NoteAddCommand.MESSAGE_DUPLICATE_NOTE);
    }

    @Test
    public void execute_invalidStudentOrNoteIndex_failureWithoutChanges() {
        Index missingStudent = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditNoteDescriptor descriptor = descriptor(null, "Changed");
        assertCommandFailure(new NoteEditCommand(missingStudent, FIRST_NOTE, descriptor), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(new NoteEditCommand(INDEX_FIRST_PERSON, FIRST_NOTE, descriptor), model,
                Messages.MESSAGE_INVALID_NOTE_DISPLAYED_INDEX);
        giveFirstStudentNotes();
        assertCommandFailure(new NoteEditCommand(INDEX_FIRST_PERSON, Index.fromOneBased(3), descriptor), model,
                Messages.MESSAGE_INVALID_NOTE_DISPLAYED_INDEX);
    }

    @Test
    public void execute_filteredList_usesDisplayedStudentIndexAndKeepsFilters() throws Exception {
        Student hidden = model.getFilteredPersonList().getFirst();
        Student target = giveNotes(model.getFilteredPersonList().get(1));
        Predicate<Lesson> lessonFilter = lesson -> false;
        model.updateFilteredPersonList(target::isSamePerson, lessonFilter);

        new NoteEditCommand(INDEX_FIRST_PERSON, FIRST_NOTE, descriptor(null, "Changed")).execute(model);

        assertEquals(List.of(target.withNotes(List.of(new ProgressNote(SEPTEMBER.getDate(), "Changed"), OCTOBER))),
                model.getFilteredPersonList());
        assertEquals(hidden, model.getAddressBook().getPersonList().getFirst());
        assertEquals(lessonFilter, model.getLessonPredicate().getValue());
    }

    @Test
    public void execute_archivedStudent_keepsArchiveStatusAndOtherDetails() throws Exception {
        Student original = giveFirstStudentNotes();
        Student archived = new PersonBuilder(original).withFollowUps("Call guardian").build().withArchived(true);
        model.setPerson(original, archived);
        new ListArchiveCommand().execute(model);

        new NoteEditCommand(INDEX_FIRST_PERSON, FIRST_NOTE, descriptor(null, "Changed")).execute(model);

        assertTrue(model.isArchiveView());
        assertEquals(archived.withNotes(List.of(new ProgressNote(SEPTEMBER.getDate(), "Changed"), OCTOBER)),
                model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_editRemovesStudentFromFilter_doesNotOpenAnotherStudent() throws Exception {
        giveFirstStudentNotes();
        model.updateFilteredPersonList(student -> student.getNotes().contains(SEPTEMBER));

        CommandResult result = new NoteEditCommand(INDEX_FIRST_PERSON, FIRST_NOTE,
                descriptor(null, "Changed")).execute(model);

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertTrue(result.getStudentToOpen().isEmpty());
    }

    @Test
    public void constructor_descriptor_isDefensivelyCopied() throws Exception {
        giveFirstStudentNotes();
        EditNoteDescriptor descriptor = descriptor(null, "Changed");
        NoteEditCommand command = new NoteEditCommand(INDEX_FIRST_PERSON, FIRST_NOTE, descriptor);
        descriptor.setText("Later change");

        command.execute(model);

        assertEquals("Changed", model.getFilteredPersonList().getFirst().getNotes().getFirst().getText());
    }

    @Test
    public void equals() {
        EditNoteDescriptor descriptor = descriptor(null, "Changed");
        NoteEditCommand command = new NoteEditCommand(INDEX_FIRST_PERSON, FIRST_NOTE, descriptor);
        NoteEditCommand copy = new NoteEditCommand(INDEX_FIRST_PERSON, FIRST_NOTE, new EditNoteDescriptor(descriptor));
        assertEquals(command, command);
        assertEquals(command, copy);
        assertEquals(command.hashCode(), copy.hashCode());
        assertNotEquals(command, new NoteEditCommand(INDEX_SECOND_PERSON, FIRST_NOTE, descriptor));
        assertNotEquals(command, new NoteEditCommand(INDEX_FIRST_PERSON, SECOND_NOTE, descriptor));
        assertNotEquals(command, new NoteEditCommand(INDEX_FIRST_PERSON, FIRST_NOTE, descriptor(null, "Other")));
        assertNotEquals(command, new NoteEditCommand(INDEX_FIRST_PERSON, FIRST_NOTE,
                descriptor(SEPTEMBER.getDate(), null)));
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand());
    }
}
