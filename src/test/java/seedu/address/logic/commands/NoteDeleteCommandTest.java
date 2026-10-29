//@@author pranavp311
package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.Student;
import seedu.address.testutil.PersonBuilder;

public class NoteDeleteCommandTest {
    private static final ProgressNote OCTOBER = new ProgressNote(LocalDate.of(2026, 10, 15), "Fractions");
    private static final ProgressNote SEPTEMBER = new ProgressNote(LocalDate.of(2026, 9, 1), "Placement test");
    private static final Index FIRST_NOTE = Index.fromOneBased(1);
    private static final Index SECOND_NOTE = Index.fromOneBased(2);

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private static NoteDeleteCommand delete(Index student, Index note) {
        return new NoteDeleteCommand(student, note);
    }

    private Model expectedModelWith(Student original, Student replacement) {
        Model expected = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expected.setPerson(original, replacement);
        return expected;
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new NoteDeleteCommand(null, FIRST_NOTE));
        assertThrows(NullPointerException.class, () -> new NoteDeleteCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_delete_removesOnlyThatNoteAndOpensStudent() throws Exception {
        Student original = model.getFilteredPersonList().get(0);
        model.setPerson(original, new PersonBuilder(original).withNotes(OCTOBER, SEPTEMBER).build());
        Student first = model.getFilteredPersonList().get(0);
        Student expectedStudent = new PersonBuilder(first).withNotes(OCTOBER).build();
        CommandResult expectedResult = new CommandResult(
                String.format(NoteDeleteCommand.MESSAGE_DELETE_NOTE_SUCCESS, 1, first.getName(), SEPTEMBER),
                INDEX_FIRST_PERSON);
        // note 1 is the September note, since notes are numbered oldest first
        assertCommandSuccess(delete(INDEX_FIRST_PERSON, FIRST_NOTE), model, expectedResult,
                expectedModelWith(first, expectedStudent));
    }

    @Test
    public void execute_deleteInvalidIndex_failure() throws Exception {
        assertCommandFailure(delete(INDEX_FIRST_PERSON, FIRST_NOTE), model,
                Messages.MESSAGE_INVALID_NOTE_DISPLAYED_INDEX);
        Student original = model.getFilteredPersonList().get(0);
        model.setPerson(original, new PersonBuilder(original).withNotes(OCTOBER).build());
        assertCommandFailure(delete(INDEX_FIRST_PERSON, SECOND_NOTE), model,
                Messages.MESSAGE_INVALID_NOTE_DISPLAYED_INDEX);
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(delete(outOfBounds, FIRST_NOTE), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    //@@author
    @Test
    public void equals() {
        Command deleteFirst = delete(INDEX_FIRST_PERSON, FIRST_NOTE);
        assertEquals(deleteFirst, delete(INDEX_FIRST_PERSON, FIRST_NOTE));
        assertNotEquals(deleteFirst, delete(INDEX_FIRST_PERSON, SECOND_NOTE));
        assertNotEquals(deleteFirst, delete(INDEX_SECOND_PERSON, FIRST_NOTE));
        assertNotEquals(deleteFirst, new ClearCommand());
    }
}
