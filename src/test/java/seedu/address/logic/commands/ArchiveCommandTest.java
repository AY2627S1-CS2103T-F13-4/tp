package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Student;
import seedu.address.testutil.AddressBookBuilder;

/**
 * Contains integration tests for {@code ArchiveCommand} and the displayed student list.
 */
public class ArchiveCommandTest {

    private Model model = new ModelManager(new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build(),
            new UserPrefs());

    @Test
    public void execute_afterList_removesPersonFromDisplayedList() throws CommandException {
        new ListCommand().execute(model);

        CommandResult commandResult = new ArchiveCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(String.format(ArchiveCommand.MESSAGE_ARCHIVE_PERSON_SUCCESS, Messages.format(ALICE)),
                commandResult.getFeedbackToUser());
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertTrue(model.getAddressBook().getPersonList().get(0).isArchived());
        assertFalse(model.getAddressBook().getPersonList().get(1).isArchived());
    }

    @Test
    public void execute_afterAdd_removesPersonFromDisplayedList() throws CommandException {
        model = new ModelManager();
        new AddCommand(ALICE).execute(model);

        new ArchiveCommand(INDEX_FIRST_PERSON).execute(model);

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertTrue(model.getAddressBook().getPersonList().get(0).isArchived());
    }

    @Test
    public void execute_consecutiveArchives_removesEachPersonFromDisplayedList() throws CommandException {
        new ListCommand().execute(model);

        new ArchiveCommand(INDEX_FIRST_PERSON).execute(model);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());

        CommandResult commandResult = new ArchiveCommand(INDEX_FIRST_PERSON).execute(model);

        assertEquals(String.format(ArchiveCommand.MESSAGE_ARCHIVE_PERSON_SUCCESS, Messages.format(BENSON)),
                commandResult.getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(2, model.getAddressBook().getPersonList().size());
        assertTrue(model.getAddressBook().getPersonList().stream().allMatch(Student::isArchived));

        new ListCommand().execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new ArchiveCommand(outOfBoundIndex), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertTrue(INDEX_SECOND_PERSON.getZeroBased() < model.getAddressBook().getPersonList().size());

        assertCommandFailure(new ArchiveCommand(INDEX_SECOND_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }
}
