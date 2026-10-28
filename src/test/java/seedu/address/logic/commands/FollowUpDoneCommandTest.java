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

public class FollowUpDoneCommandTest {
    private static final FollowUp REPORT = new FollowUp("Send the term 4 progress report");
    private static final FollowUp MAKE_UP = new FollowUp("Confirm the make-up lesson");
    private static final Index FIRST_ITEM = Index.fromOneBased(1);
    private static final Index SECOND_ITEM = Index.fromOneBased(2);
    private static final Index THIRD_ITEM = Index.fromOneBased(3);

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private static FollowUpDoneCommand done(Index student, Index item) {
        return new FollowUpDoneCommand(student, item);
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
        assertThrows(NullPointerException.class, () -> new FollowUpDoneCommand(null, FIRST_ITEM));
        assertThrows(NullPointerException.class, () -> new FollowUpDoneCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_done_removesOnlyThatItemAndShowsIt() {
        Student original = giveFirstStudentTwoItems();
        Student expectedStudent = new PersonBuilder(original).withFollowUps(MAKE_UP.getText()).build();
        CommandResult expectedResult = new CommandResult(
                String.format(FollowUpDoneCommand.MESSAGE_DONE_FOLLOW_UP_SUCCESS, 1, original.getName(), REPORT),
                INDEX_FIRST_PERSON);
        assertCommandSuccess(done(INDEX_FIRST_PERSON, FIRST_ITEM), model, expectedResult,
                expectedModelWith(original, expectedStudent));
    }

    @Test
    public void execute_doneLastItemAfterFollowUpsList_studentLeavesListAndIsNotOpened() throws Exception {
        Student first = model.getFilteredPersonList().get(0);
        model.setPerson(first, new PersonBuilder(first).withFollowUps(REPORT.getText()).build());
        Student second = model.getFilteredPersonList().get(1);
        model.setPerson(second, new PersonBuilder(second).withFollowUps(MAKE_UP.getText()).build());
        new FollowUpListCommand().execute(model);
        assertEquals(2, model.getFilteredPersonList().size());

        CommandResult result = done(INDEX_FIRST_PERSON, FIRST_ITEM).execute(model);

        // The second student now holds number 1, so opening number 1 would show the wrong student.
        assertEquals(new CommandResult(String.format(FollowUpDoneCommand.MESSAGE_DONE_FOLLOW_UP_SUCCESS,
                1, first.getName(), REPORT),
                CommandResult.Screen.STUDENTS), result);
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(second.getName(), model.getFilteredPersonList().get(0).getName());
    }

    @Test
    public void execute_invalidStudentIndex_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new FollowUpDoneCommand(INDEX_SECOND_PERSON, FIRST_ITEM), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidItemIndex_failure() {
        giveFirstStudentTwoItems();
        assertCommandFailure(new FollowUpDoneCommand(INDEX_FIRST_PERSON, THIRD_ITEM), model,
                Messages.MESSAGE_INVALID_FOLLOW_UP_DISPLAYED_INDEX);
        // The second student has no follow-ups.
        assertCommandFailure(done(INDEX_SECOND_PERSON, FIRST_ITEM), model,
                Messages.MESSAGE_INVALID_FOLLOW_UP_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        FollowUpDoneCommand command = done(INDEX_FIRST_PERSON, FIRST_ITEM);
        assertEquals(command, done(INDEX_FIRST_PERSON, FIRST_ITEM));
        assertEquals(command.hashCode(), done(INDEX_FIRST_PERSON, FIRST_ITEM).hashCode());
        assertNotEquals(command, done(INDEX_FIRST_PERSON, SECOND_ITEM));
        assertNotEquals(command, done(INDEX_SECOND_PERSON, FIRST_ITEM));
        assertNotEquals(command, null);
        assertNotEquals(command, new FollowUpListCommand());
    }
}
