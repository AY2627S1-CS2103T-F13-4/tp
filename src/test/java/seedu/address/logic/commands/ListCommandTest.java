package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Student;

/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 */
public class ListCommandTest {

    /** list shows the student table, wherever the user was. */
    private static final CommandResult SHOWS_TABLE =
            new CommandResult(ListCommand.MESSAGE_SUCCESS, CommandResult.Screen.STUDENTS);

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(new ListCommand(), model, SHOWS_TABLE, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandSuccess(new ListCommand(), model, SHOWS_TABLE, expectedModel);
    }

    @Test
    public void execute_afterListArchive_showsOnlyActiveStudents() {
        Student person = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Student archivedPerson = person.withArchived(true);
        model.setPerson(person, archivedPerson);
        new ListArchiveCommand().execute(model);
        expectedModel.setPerson(person, archivedPerson);
        expectedModel.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        assertCommandSuccess(new ListCommand(), model, SHOWS_TABLE, expectedModel);
    }
}
