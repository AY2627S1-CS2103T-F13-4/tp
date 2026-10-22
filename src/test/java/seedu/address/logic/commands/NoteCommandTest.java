package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.NoteCommand.Operation;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.School;
import seedu.address.model.person.Student;
import seedu.address.testutil.PersonBuilder;

public class NoteCommandTest {
    private static final ProgressNote OCTOBER = new ProgressNote(LocalDate.of(2026, 10, 15), "Fractions");
    private static final ProgressNote SEPTEMBER = new ProgressNote(LocalDate.of(2026, 9, 1), "Placement test");

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private static NoteCommand add(Index student, ProgressNote note) {
        return new NoteCommand(student, Operation.ADD, note);
    }

    private Model expectedModelWith(Student original, Student replacement) {
        Model expected = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expected.setPerson(original, replacement);
        return expected;
    }

    @Test
    public void execute_add_savesNoteAndOpensStudent() {
        Student first = model.getFilteredPersonList().get(0);
        Student expectedStudent = new PersonBuilder(first).withNotes(OCTOBER).build();
        CommandResult expectedResult = new CommandResult(
                String.format(NoteCommand.MESSAGE_ADDED, 1, first.getName()), INDEX_FIRST_PERSON);
        assertCommandSuccess(add(INDEX_FIRST_PERSON, OCTOBER), model, expectedResult,
                expectedModelWith(first, expectedStudent));
    }

    @Test
    public void execute_add_keepsSchool() throws Exception {
        Student first = model.getFilteredPersonList().get(0);
        model.setPerson(first, new PersonBuilder(first).withSchool(new School("Raffles Institution")).build());
        add(INDEX_FIRST_PERSON, OCTOBER).execute(model);

        assertEquals(Optional.of(new School("Raffles Institution")),
                model.getFilteredPersonList().get(0).getSchool());
    }

    @Test
    public void execute_addEarlierNote_numberedByDate() throws Exception {
        add(INDEX_FIRST_PERSON, OCTOBER).execute(model);
        CommandResult result = add(INDEX_FIRST_PERSON, SEPTEMBER).execute(model);

        Student first = model.getFilteredPersonList().get(0);
        assertEquals(String.format(NoteCommand.MESSAGE_ADDED, 1, first.getName()), result.getFeedbackToUser());
        assertEquals(List.of(SEPTEMBER, OCTOBER), first.getNotes());
    }

    @Test
    public void execute_filteredList_usesDisplayedIndex() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Student second = model.getFilteredPersonList().get(0);
        Student expectedStudent = new PersonBuilder(second).withNotes(OCTOBER).build();
        Model expectedModel = expectedModelWith(second, expectedStudent);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        CommandResult expectedResult = new CommandResult(
                String.format(NoteCommand.MESSAGE_ADDED, 1, second.getName()), INDEX_FIRST_PERSON);
        assertCommandSuccess(add(INDEX_FIRST_PERSON, OCTOBER), model, expectedResult, expectedModel);
    }

    @Test
    public void execute_invalidStudentIndex_failure() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(add(outOfBounds, OCTOBER), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_duplicateNote_failure() throws Exception {
        add(INDEX_FIRST_PERSON, OCTOBER).execute(model);
        assertCommandFailure(add(INDEX_FIRST_PERSON, OCTOBER), model, NoteCommand.MESSAGE_DUPLICATE_NOTE);
    }

    /** Commands that rebuild a student must carry the notes over, or editing anything else would lose them. */
    @Test
    public void otherStudentCommands_keepNotes() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        add(INDEX_FIRST_PERSON, OCTOBER).execute(model);
        for (String command : List.of("edit 1 phone/98765432", "guardian-add 1 name/Janet Tan phone/91234567",
                "guardian-edit 1 phone/87654321", "lesson-add 1 subject/Math slot/Mon 4.00pm 5.00pm rate/40",
                "lesson-edit 1 1 rate/45", "lesson-delete 1 1", "guardian-delete 1")) {
            parser.parseCommand(command).execute(model);
            assertEquals(List.of(OCTOBER), model.getFilteredPersonList().get(0).getNotes(), command);
        }
    }

    @Test
    public void equals() {
        NoteCommand addFirst = add(INDEX_FIRST_PERSON, OCTOBER);
        assertEquals(addFirst, add(INDEX_FIRST_PERSON, OCTOBER));
        assertEquals(addFirst.hashCode(), add(INDEX_FIRST_PERSON, OCTOBER).hashCode());
        assertNotEquals(addFirst, add(INDEX_SECOND_PERSON, OCTOBER));
        assertNotEquals(addFirst, add(INDEX_FIRST_PERSON, SEPTEMBER));
        assertNotEquals(addFirst, null);
    }
}
