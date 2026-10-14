package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Student;

public class ViewCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    // view opens a student from the keyboard, e.g. the "#2" of a calendar lesson, without changing any data.
    @Test
    public void execute_validIndex_opensThatStudentInTable() {
        Student second = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        CommandResult expected = new CommandResult(
                String.format(ViewCommand.MESSAGE_SUCCESS, second.getName()), INDEX_SECOND_PERSON);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandSuccess(new ViewCommand(INDEX_SECOND_PERSON), model, expected, expectedModel);
        assertEquals(CommandResult.Screen.STUDENTS, expected.getScreen());
    }

    // After a find, the index counts the shown students, like every other command.
    @Test
    public void execute_filteredList_usesShownIndex() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Student shown = model.getFilteredPersonList().get(0);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);

        assertCommandSuccess(new ViewCommand(INDEX_FIRST_PERSON), model,
                new CommandResult(String.format(ViewCommand.MESSAGE_SUCCESS, shown.getName()), INDEX_FIRST_PERSON),
                expectedModel);
        assertCommandFailure(new ViewCommand(INDEX_SECOND_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_indexPastList_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new ViewCommand(outOfBounds), model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        ViewCommand first = new ViewCommand(INDEX_FIRST_PERSON);
        assertTrue(first.equals(first));
        assertTrue(first.equals(new ViewCommand(INDEX_FIRST_PERSON)));
        assertFalse(first.equals(new ViewCommand(INDEX_SECOND_PERSON)));
        assertFalse(first.equals(null));
        assertFalse(first.equals(1));
    }
}
