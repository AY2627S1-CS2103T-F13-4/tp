package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class WeekCommandTest {

    private static final CommandResult SHOWS_WEEK =
            new CommandResult(WeekCommand.MESSAGE_SUCCESS, CommandResult.Screen.WEEK);

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_listNotFiltered_showsWeek() {
        assertCommandSuccess(new WeekCommand(), model, SHOWS_WEEK, expectedModel);
    }

    @Test
    public void execute_listFiltered_keepsSearch() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);
        assertCommandSuccess(new WeekCommand(), model, SHOWS_WEEK, expectedModel);
    }
}
