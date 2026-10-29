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

public class FollowUpAddCommandTest {
    private static final FollowUp REPORT = new FollowUp("Send the term 4 progress report");
    private static final FollowUp MAKE_UP = new FollowUp("Confirm the make-up lesson");
    private static final Index FIRST_ITEM = Index.fromOneBased(1);

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private static FollowUpAddCommand add(Index student, FollowUp followUp) {
        return new FollowUpAddCommand(student, followUp);
    }

    private static FollowUpEditCommand edit(Index student, Index item, FollowUp followUp) {
        return new FollowUpEditCommand(student, item, followUp);
    }

    private Model expectedModelWith(Student original, Student replacement) {
        Model expected = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expected.setPerson(original, replacement);
        return expected;
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FollowUpAddCommand(null, REPORT));
        assertThrows(NullPointerException.class, () -> new FollowUpAddCommand(INDEX_FIRST_PERSON, null));
    }

    @Test
    public void execute_add_appendsItemAndOpensStudent() {
        Student first = model.getFilteredPersonList().get(0);
        Student expectedStudent = new PersonBuilder(first).withFollowUps(REPORT.getText()).build();
        CommandResult expectedResult = new CommandResult(
                String.format(FollowUpAddCommand.MESSAGE_SUCCESS, 1, first.getName(), REPORT), INDEX_FIRST_PERSON);
        assertCommandSuccess(add(INDEX_FIRST_PERSON, REPORT), model, expectedResult,
                expectedModelWith(first, expectedStudent));
    }

    @Test
    public void execute_addSecond_numberedAfterExistingItems() throws Exception {
        add(INDEX_FIRST_PERSON, REPORT).execute(model);
        CommandResult result = add(INDEX_FIRST_PERSON, MAKE_UP).execute(model);

        Student first = model.getFilteredPersonList().get(0);
        assertEquals(List.of(REPORT, MAKE_UP), first.getFollowUps());
        assertEquals(String.format(FollowUpAddCommand.MESSAGE_SUCCESS, 2, first.getName(), MAKE_UP),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_addDuplicateIgnoringCase_failure() throws Exception {
        add(INDEX_FIRST_PERSON, REPORT).execute(model);
        FollowUp shouted = new FollowUp(REPORT.getText().toUpperCase());
        assertCommandFailure(add(INDEX_FIRST_PERSON, shouted), model, FollowUpAddCommand.MESSAGE_DUPLICATE_FOLLOW_UP);
    }

    @Test
    public void execute_addWithoutGuardian_success() throws Exception {
        Student first = model.getFilteredPersonList().get(0);
        model.setPerson(first, new PersonBuilder(first).withGuardian(null).build());

        add(INDEX_FIRST_PERSON, REPORT).execute(model);

        assertEquals(List.of(REPORT), model.getFilteredPersonList().get(0).getFollowUps());
    }

    @Test
    public void execute_invalidStudentIndex_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new FollowUpAddCommand(INDEX_SECOND_PERSON, REPORT), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        Command addReport = add(INDEX_FIRST_PERSON, REPORT);
        assertEquals(addReport, addReport);
        assertEquals(addReport, add(INDEX_FIRST_PERSON, REPORT));
        assertEquals(addReport.hashCode(), add(INDEX_FIRST_PERSON, REPORT).hashCode());
        assertNotEquals(addReport, add(INDEX_SECOND_PERSON, REPORT));
        assertNotEquals(addReport, add(INDEX_FIRST_PERSON, MAKE_UP));
        assertNotEquals(addReport, edit(INDEX_FIRST_PERSON, FIRST_ITEM, REPORT));
        assertNotEquals(addReport, null);
        assertNotEquals(addReport, new FollowUpListCommand());
    }
}
