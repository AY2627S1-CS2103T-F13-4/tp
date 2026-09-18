package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

public class RemarkCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addReplaceAndClear_success() {
        assertRemarkSuccess("Likes swimming");
        assertRemarkSuccess("Prefers cycling");
        assertRemarkSuccess("");
    }

    @Test
    public void execute_filteredList_updatesDisplayedPerson() {
        Person target = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        assertRemarkSuccess("A note");
        assertEquals(new PersonBuilder(target).withRemark("A note").build(),
                model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased()));
    }

    @Test
    public void execute_invalidIndex_failure() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new RemarkCommand(outOfBounds, new Remark("note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("note")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void edit_otherFields_preservesRemark() throws Exception {
        new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Keep this note")).execute(model);
        new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptorBuilder().withPhone("87654321").build())
                .execute(model);
        assertEquals(new Remark("Keep this note"), model.getFilteredPersonList().get(0).getRemark());
    }

    @Test
    public void equals() {
        RemarkCommand command = new RemarkCommand(INDEX_FIRST_PERSON, new Remark("note"));
        assertEquals(command, command);
        assertEquals(command, new RemarkCommand(INDEX_FIRST_PERSON, new Remark("note")));
        assertNotEquals(command, new RemarkCommand(INDEX_SECOND_PERSON, new Remark("note")));
        assertNotEquals(command, new RemarkCommand(INDEX_FIRST_PERSON, new Remark("other")));
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand());
    }

    private void assertRemarkSuccess(String remark) {
        Person original = model.getFilteredPersonList().get(0);
        Person edited = new PersonBuilder(original).withRemark(remark).build();
        Model expected = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expected.setPerson(original, edited);
        String message = remark.isEmpty() ? RemarkCommand.MESSAGE_DELETE_REMARK_SUCCESS
                : RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS;
        assertCommandSuccess(new RemarkCommand(INDEX_FIRST_PERSON, new Remark(remark)), model,
                String.format(message, Messages.format(edited)), expected);
    }
}
