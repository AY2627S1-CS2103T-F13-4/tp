package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Student;
import seedu.address.testutil.PersonBuilder;

public class FollowUpEditCommandTest {
    private static final FollowUp REPORT = new FollowUp("Send the term 4 progress report");
    private static final FollowUp MAKE_UP = new FollowUp("Confirm the make-up lesson");
    private static final Index FIRST_ITEM = Index.fromOneBased(1);
    private static final Index SECOND_ITEM = Index.fromOneBased(2);
    private static final Index THIRD_ITEM = Index.fromOneBased(3);

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private static FollowUpEditCommand edit(Index student, Index item, FollowUp followUp) {
        return new FollowUpEditCommand(student, item, followUp);
    }

    private Model expectedModelWith(Student original, Student replacement) {
        Model expected = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expected.setPerson(original, replacement);
        return expected;
    }

    /** Gives the first displayed student the follow-ups REPORT then MAKE_UP, and returns that student. */
    private Student giveFirstStudentTwoItems() {
        Student first = model.getFilteredPersonList().get(0);
        Student withItems = new PersonBuilder(first)
                .withFollowUps(REPORT.getText(), MAKE_UP.getText()).build();
        model.setPerson(first, withItems);
        return withItems;
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FollowUpEditCommand(null, FIRST_ITEM, REPORT));
        assertThrows(NullPointerException.class, () -> new FollowUpEditCommand(INDEX_FIRST_PERSON, null, REPORT));
        assertThrows(NullPointerException.class, () -> new FollowUpEditCommand(INDEX_FIRST_PERSON, FIRST_ITEM, null));
    }

    @Test
    public void execute_edit_replacesOnlyThatItem() {
        Student original = giveFirstStudentTwoItems();
        FollowUp changed = new FollowUp("Confirm the make-up lesson on Saturday");
        Student expectedStudent = new PersonBuilder(original)
                .withFollowUps(REPORT.getText(), changed.getText()).build();
        CommandResult expectedResult = new CommandResult(
                String.format(FollowUpEditCommand.MESSAGE_EDIT_FOLLOW_UP_SUCCESS, 2, original.getName(), changed),
                INDEX_FIRST_PERSON);
        assertCommandSuccess(edit(INDEX_FIRST_PERSON, SECOND_ITEM, changed), model, expectedResult,
                expectedModelWith(original, expectedStudent));
    }

    @Test
    public void execute_editToOwnTextInNewCase_success() throws Exception {
        giveFirstStudentTwoItems();
        FollowUp recased = new FollowUp(REPORT.getText().toUpperCase());

        edit(INDEX_FIRST_PERSON, FIRST_ITEM, recased).execute(model);

        assertEquals(List.of(recased, MAKE_UP), model.getFilteredPersonList().get(0).getFollowUps());
    }

    @Test
    public void execute_editToAnotherItemsText_failure() {
        giveFirstStudentTwoItems();
        assertCommandFailure(edit(INDEX_FIRST_PERSON, FIRST_ITEM, MAKE_UP), model,
                FollowUpAddCommand.MESSAGE_DUPLICATE_FOLLOW_UP);
    }

    @Test
    public void execute_invalidStudentIndex_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new FollowUpEditCommand(INDEX_SECOND_PERSON, FIRST_ITEM, REPORT), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidItemIndex_failure() {
        giveFirstStudentTwoItems();
        assertCommandFailure(new FollowUpEditCommand(INDEX_FIRST_PERSON, THIRD_ITEM, REPORT), model,
                Messages.MESSAGE_INVALID_FOLLOW_UP_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        FollowUpEditCommand command = edit(INDEX_FIRST_PERSON, FIRST_ITEM, REPORT);
        assertEquals(command, edit(INDEX_FIRST_PERSON, FIRST_ITEM, REPORT));
        assertEquals(command.hashCode(), edit(INDEX_FIRST_PERSON, FIRST_ITEM, REPORT).hashCode());
        assertNotEquals(command, edit(INDEX_SECOND_PERSON, FIRST_ITEM, REPORT));
        assertNotEquals(command, edit(INDEX_FIRST_PERSON, SECOND_ITEM, REPORT));
        assertNotEquals(command, edit(INDEX_FIRST_PERSON, FIRST_ITEM, MAKE_UP));
        assertNotEquals(command, null);
        assertNotEquals(command, new ClearCommand());
    }
}
