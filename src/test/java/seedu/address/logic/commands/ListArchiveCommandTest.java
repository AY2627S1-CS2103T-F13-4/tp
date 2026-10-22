package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Student;
import seedu.address.testutil.AddressBookBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code ListArchiveCommand}.
 */
public class ListArchiveCommandTest {

    private static final CommandResult SHOWS_TABLE =
            new CommandResult(ListArchiveCommand.MESSAGE_SUCCESS, CommandResult.Screen.STUDENTS);

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON.withArchived(true))
                .withPerson(CARL.withArchived(true)).build(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(Student::isArchived);
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListArchiveCommand().execute(null));
    }

    @Test
    public void execute_listIsNotFiltered_showsOnlyArchivedStudents() {
        assertCommandSuccess(new ListArchiveCommand(), model, SHOWS_TABLE, expectedModel);
        assertEquals(List.of(BENSON.withArchived(true), CARL.withArchived(true)), model.getFilteredPersonList());
    }

    @Test
    public void execute_listIsFiltered_showsAllArchivedStudents() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertCommandSuccess(new ListArchiveCommand(), model, SHOWS_TABLE, expectedModel);
    }

    @Test
    public void execute_lessonsAreFiltered_clearsLessonFilter() {
        model.updateFilteredPersonList(student -> true, lesson -> false);

        assertCommandSuccess(new ListArchiveCommand(), model, SHOWS_TABLE, expectedModel);
    }

    @Test
    public void execute_emptyAddressBook_showsEmptyList() {
        model = new ModelManager();
        expectedModel = new ModelManager();

        assertCommandSuccess(new ListArchiveCommand(), model, SHOWS_TABLE, expectedModel);
    }

    @Test
    public void execute_noArchivedStudents_showsEmptyList() {
        model = new ModelManager(new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build(),
                new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(student -> false);

        assertCommandSuccess(new ListArchiveCommand(), model, SHOWS_TABLE, expectedModel);
    }

    @Test
    public void execute_allStudentsArchived_showsAllStudents() {
        model = new ModelManager(new AddressBookBuilder().withPerson(ALICE.withArchived(true))
                .withPerson(BENSON.withArchived(true)).build(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandSuccess(new ListArchiveCommand(), model, SHOWS_TABLE, expectedModel);
    }
}
