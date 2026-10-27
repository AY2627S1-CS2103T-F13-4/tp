package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Lesson;
import seedu.address.testutil.AddressBookBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code ListArchiveCommand}.
 */
public class ListArchiveCommandTest {

    private static final CommandResult SHOWS_TABLE =
            new CommandResult(ListArchiveCommand.MESSAGE_SUCCESS, CommandResult.Screen.ARCHIVE);

    private Model model;
    private Model expectedModel;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON.withArchived(true))
                .withPerson(CARL.withArchived(true)).build(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setArchiveView(true);
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListArchiveCommand().execute(null));
    }

    @Test
    public void execute_listIsNotFiltered_showsOnlyArchivedStudents() {
        assertCommandSuccess(new ListArchiveCommand(), model, SHOWS_TABLE, expectedModel);
        assertEquals(List.of(BENSON.withArchived(true), CARL.withArchived(true)), model.getFilteredPersonList());
        assertEquals(List.of(ALICE), model.getFilteredActivePersonList());
        assertTrue(model.isArchiveView());
    }

    @Test
    public void execute_listIsFiltered_showsAllArchivedStudents() {
        new ListArchiveCommand().execute(model);
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertCommandSuccess(new ListArchiveCommand(), model, SHOWS_TABLE, expectedModel);
    }

    @Test
    public void execute_activeLessonsAreFiltered_preservesActiveLessonFilter() {
        Predicate<Lesson> lessonPredicate = lesson -> false;
        model.updateFilteredPersonList(student -> true, lessonPredicate);

        assertEquals(SHOWS_TABLE, new ListArchiveCommand().execute(model));
        assertEquals(List.of(BENSON.withArchived(true), CARL.withArchived(true)), model.getFilteredPersonList());
        assertEquals(lessonPredicate, model.getLessonPredicate().getValue());
    }

    @Test
    public void execute_emptyAddressBook_showsEmptyList() {
        model = new ModelManager();
        expectedModel = new ModelManager();
        expectedModel.setArchiveView(true);

        assertCommandSuccess(new ListArchiveCommand(), model, SHOWS_TABLE, expectedModel);
    }

    @Test
    public void execute_noArchivedStudents_showsEmptyList() {
        model = new ModelManager(new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build(),
                new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setArchiveView(true);

        assertCommandSuccess(new ListArchiveCommand(), model, SHOWS_TABLE, expectedModel);
    }

    @Test
    public void execute_allStudentsArchived_showsAllStudents() {
        model = new ModelManager(new AddressBookBuilder().withPerson(ALICE.withArchived(true))
                .withPerson(BENSON.withArchived(true)).build(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setArchiveView(true);

        assertCommandSuccess(new ListArchiveCommand(), model, SHOWS_TABLE, expectedModel);
    }
}
