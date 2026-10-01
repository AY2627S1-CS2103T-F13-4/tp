package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;

public class RemarkCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndex_success() {
        Remark remark = new Remark("Likes to swim.");
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new Person(personToEdit.getName(), personToEdit.getPhone(), personToEdit.getEmail(),
                personToEdit.getAddress(), remark, personToEdit.getTags());
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, remark);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);
        String expectedMessage = String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS,
                Messages.format(editedPerson));

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outOfBoundsIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        RemarkCommand command = new RemarkCommand(outOfBoundsIndex, new Remark("Remark"));
        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        RemarkCommand firstCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("One"));
        RemarkCommand firstCommandCopy = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("One"));
        RemarkCommand differentIndex = new RemarkCommand(INDEX_SECOND_PERSON, new Remark("One"));
        RemarkCommand differentRemark = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Two"));

        assertTrue(firstCommand.equals(firstCommand));
        assertTrue(firstCommand.equals(firstCommandCopy));
        assertFalse(firstCommand.equals(differentIndex));
        assertFalse(firstCommand.equals(differentRemark));
        assertFalse(firstCommand.equals(null));
    }
}
