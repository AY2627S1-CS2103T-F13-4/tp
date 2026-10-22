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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.FollowUpCommand.Operation;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.Student;
import seedu.address.testutil.PersonBuilder;

public class FollowUpCommandTest {
    private static final FollowUp REPORT = new FollowUp("Send the term 4 progress report");
    private static final FollowUp MAKE_UP = new FollowUp("Confirm the make-up lesson");
    private static final Index FIRST_ITEM = Index.fromOneBased(1);
    private static final Index SECOND_ITEM = Index.fromOneBased(2);
    private static final Index THIRD_ITEM = Index.fromOneBased(3);

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private static FollowUpCommand add(Index student, FollowUp followUp) {
        return new FollowUpCommand(student, Operation.ADD, Optional.empty(), Optional.of(followUp));
    }

    private static FollowUpCommand edit(Index student, Index item, FollowUp followUp) {
        return new FollowUpCommand(student, Operation.EDIT, Optional.of(item), Optional.of(followUp));
    }

    private static FollowUpCommand done(Index student, Index item) {
        return new FollowUpCommand(student, Operation.DONE, Optional.of(item), Optional.empty());
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
    public void constructor_invalidFields_throwsIllegalArgumentException() {
        Optional<Index> item = Optional.of(FIRST_ITEM);
        Optional<FollowUp> text = Optional.of(REPORT);
        assertThrows(IllegalArgumentException.class, () ->
                new FollowUpCommand(INDEX_FIRST_PERSON, Operation.ADD, item, text));
        assertThrows(IllegalArgumentException.class, () ->
                new FollowUpCommand(INDEX_FIRST_PERSON, Operation.ADD, Optional.empty(), Optional.empty()));
        assertThrows(IllegalArgumentException.class, () ->
                new FollowUpCommand(INDEX_FIRST_PERSON, Operation.EDIT, Optional.empty(), text));
        assertThrows(IllegalArgumentException.class, () ->
                new FollowUpCommand(INDEX_FIRST_PERSON, Operation.EDIT, item, Optional.empty()));
        assertThrows(IllegalArgumentException.class, () ->
                new FollowUpCommand(INDEX_FIRST_PERSON, Operation.DONE, item, text));
        assertThrows(IllegalArgumentException.class, () ->
                new FollowUpCommand(INDEX_FIRST_PERSON, Operation.DONE, Optional.empty(), Optional.empty()));
    }

    @Test
    public void execute_add_appendsItemAndOpensStudent() {
        Student first = model.getFilteredPersonList().get(0);
        Student expectedStudent = new PersonBuilder(first).withFollowUps(REPORT.getText()).build();
        CommandResult expectedResult = new CommandResult(
                String.format(FollowUpCommand.MESSAGE_ADDED, 1, first.getName(), REPORT), INDEX_FIRST_PERSON);
        assertCommandSuccess(add(INDEX_FIRST_PERSON, REPORT), model, expectedResult,
                expectedModelWith(first, expectedStudent));
    }

    @Test
    public void execute_addSecond_numberedAfterExistingItems() throws Exception {
        add(INDEX_FIRST_PERSON, REPORT).execute(model);
        CommandResult result = add(INDEX_FIRST_PERSON, MAKE_UP).execute(model);

        Student first = model.getFilteredPersonList().get(0);
        assertEquals(List.of(REPORT, MAKE_UP), first.getFollowUps());
        assertEquals(String.format(FollowUpCommand.MESSAGE_ADDED, 2, first.getName(), MAKE_UP),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_addDuplicateIgnoringCase_failure() throws Exception {
        add(INDEX_FIRST_PERSON, REPORT).execute(model);
        FollowUp shouted = new FollowUp(REPORT.getText().toUpperCase());
        assertCommandFailure(add(INDEX_FIRST_PERSON, shouted), model, FollowUpCommand.MESSAGE_DUPLICATE_FOLLOW_UP);
    }

    @Test
    public void execute_addWithoutGuardian_success() throws Exception {
        Student first = model.getFilteredPersonList().get(0);
        model.setPerson(first, new PersonBuilder(first).withGuardian(null).build());

        add(INDEX_FIRST_PERSON, REPORT).execute(model);

        assertEquals(List.of(REPORT), model.getFilteredPersonList().get(0).getFollowUps());
    }

    @Test
    public void execute_edit_replacesOnlyThatItem() {
        Student original = giveFirstStudentTwoItems();
        FollowUp changed = new FollowUp("Confirm the make-up lesson on Saturday");
        Student expectedStudent = new PersonBuilder(original)
                .withFollowUps(REPORT.getText(), changed.getText()).build();
        CommandResult expectedResult = new CommandResult(
                String.format(FollowUpCommand.MESSAGE_EDITED, 2, original.getName(), changed), INDEX_FIRST_PERSON);
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
                FollowUpCommand.MESSAGE_DUPLICATE_FOLLOW_UP);
    }

    @Test
    public void execute_done_removesOnlyThatItemAndShowsIt() {
        Student original = giveFirstStudentTwoItems();
        Student expectedStudent = new PersonBuilder(original).withFollowUps(MAKE_UP.getText()).build();
        CommandResult expectedResult = new CommandResult(
                String.format(FollowUpCommand.MESSAGE_DONE, 1, original.getName(), REPORT), INDEX_FIRST_PERSON);
        assertCommandSuccess(done(INDEX_FIRST_PERSON, FIRST_ITEM), model, expectedResult,
                expectedModelWith(original, expectedStudent));
    }

    @Test
    public void execute_invalidItemIndex_failure() {
        giveFirstStudentTwoItems();
        assertCommandFailure(done(INDEX_FIRST_PERSON, THIRD_ITEM), model,
                FollowUpCommand.MESSAGE_INVALID_FOLLOW_UP_DISPLAYED_INDEX);
        assertCommandFailure(edit(INDEX_FIRST_PERSON, THIRD_ITEM, REPORT), model,
                FollowUpCommand.MESSAGE_INVALID_FOLLOW_UP_DISPLAYED_INDEX);
        // The second student has no follow-ups at all.
        assertCommandFailure(done(INDEX_SECOND_PERSON, FIRST_ITEM), model,
                FollowUpCommand.MESSAGE_INVALID_FOLLOW_UP_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidStudentIndex_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(add(INDEX_SECOND_PERSON, REPORT), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(done(INDEX_SECOND_PERSON, FIRST_ITEM), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
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
        assertEquals(new CommandResult(String.format(FollowUpCommand.MESSAGE_DONE, 1, first.getName(), REPORT),
                CommandResult.Screen.STUDENTS), result);
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(second.getName(), model.getFilteredPersonList().get(0).getName());
    }

    @Test
    public void otherStudentCommands_keepFollowUps() throws Exception {
        giveFirstStudentTwoItems();
        AddressBookParser parser = new AddressBookParser();
        for (String command : List.of("edit 1 phone/90001111", "guardian-add 1 name/Janet Tan phone/91234567",
                "guardian-delete 1", "lesson-add 1 subject/Math slot/Wed 1.00pm 5.00pm rate/45",
                "note-add 1 date/2020-01-15 text/Fractions")) {
            parser.parseCommand(command).execute(model);
            assertEquals(List.of(REPORT, MAKE_UP), model.getFilteredPersonList().get(0).getFollowUps(), command);
        }
    }

    @Test
    public void archiveAndUnarchive_keepFollowUpsAndNotes() throws Exception {
        Student original = giveFirstStudentTwoItems();
        Student withNote = new PersonBuilder(original)
                .withNotes(new ProgressNote(LocalDate.of(2020, 1, 15), "Fractions")).build();
        model.setPerson(original, withNote);

        Student restored = withNote.withArchived(true).withArchived(false);

        assertEquals(withNote, restored);
        assertEquals(List.of(REPORT, MAKE_UP), restored.getFollowUps());
    }

    @Test
    public void equals() {
        FollowUpCommand addReport = add(INDEX_FIRST_PERSON, REPORT);
        assertEquals(addReport, addReport);
        assertEquals(addReport, add(INDEX_FIRST_PERSON, REPORT));
        assertEquals(addReport.hashCode(), add(INDEX_FIRST_PERSON, REPORT).hashCode());
        assertNotEquals(addReport, add(INDEX_SECOND_PERSON, REPORT));
        assertNotEquals(addReport, add(INDEX_FIRST_PERSON, MAKE_UP));
        assertNotEquals(addReport, edit(INDEX_FIRST_PERSON, FIRST_ITEM, REPORT));
        assertNotEquals(done(INDEX_FIRST_PERSON, FIRST_ITEM), done(INDEX_FIRST_PERSON, SECOND_ITEM));
        assertNotEquals(addReport, null);
        assertNotEquals(addReport, new FollowUpListCommand());
    }
}
