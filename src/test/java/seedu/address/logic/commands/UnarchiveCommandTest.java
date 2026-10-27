package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LESSON_SLOT;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code UnarchiveCommand}.
 */
public class UnarchiveCommandTest {

    private Model model = new ModelManager(
            new AddressBookBuilder().withPerson(ALICE.withArchived(true)).withPerson(BENSON).build(), new UserPrefs());

    @BeforeEach
    public void setUp() {
        new ListArchiveCommand().execute(model);
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new UnarchiveCommand(INDEX_FIRST_PERSON).execute(null));
    }

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Student personToUnarchive = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        String expectedMessage = String.format(UnarchiveCommand.MESSAGE_UNARCHIVE_PERSON_SUCCESS,
                Messages.format(personToUnarchive));
        Model expectedModel = new ModelManager(
                new AddressBookBuilder().withPerson(BENSON).withPerson(ALICE).build(), new UserPrefs());
        expectedModel.setArchiveView(true);

        assertCommandSuccess(new UnarchiveCommand(INDEX_FIRST_PERSON), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexFilteredList_restoresPersonAndKeepsArchiveView() {
        Student archivedBenson = BENSON.withArchived(true);
        model = new ModelManager(new AddressBookBuilder().withPerson(ALICE).withPerson(archivedBenson).build(),
                new UserPrefs());
        new ListArchiveCommand().execute(model);
        assertEquals(List.of(archivedBenson), model.getFilteredPersonList());

        String expectedMessage = String.format(UnarchiveCommand.MESSAGE_UNARCHIVE_PERSON_SUCCESS,
                Messages.format(archivedBenson));
        Model expectedModel = new ModelManager(
                new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build(), new UserPrefs());
        expectedModel.setArchiveView(true);

        assertCommandSuccess(new UnarchiveCommand(INDEX_FIRST_PERSON), model, expectedMessage, expectedModel);

        assertTrue(model.getFilteredPersonList().isEmpty());
        assertTrue(model.isArchiveView());
        new ListCommand().execute(model);
        assertEquals(List.of(ALICE, BENSON), model.getFilteredPersonList());
    }

    @Test
    public void execute_archivedStudentWithTuitionDetails_preservesDetails() {
        Student original = new PersonBuilder(ALICE).withSchoolingLevel(new SchoolingLevel("P5"))
                .withGuardian(new Guardian(BENSON.getName(), BENSON.getPhone()))
                .withLessons(new Lesson(new Subject("Math"), VALID_LESSON_SLOT,
                        new HourlyRate(new BigDecimal("45.50")))).build();
        Student archived = original.withArchived(true);
        model = new ModelManager(new AddressBookBuilder().withPerson(archived).build(), new UserPrefs());
        new ListArchiveCommand().execute(model);
        Model expectedModel = new ModelManager(new AddressBookBuilder().withPerson(original).build(), new UserPrefs());
        expectedModel.setArchiveView(true);
        String expectedMessage = String.format(UnarchiveCommand.MESSAGE_UNARCHIVE_PERSON_SUCCESS,
                Messages.format(archived));

        assertCommandSuccess(new UnarchiveCommand(INDEX_FIRST_PERSON), model, expectedMessage, expectedModel);
        assertTrue(archived.isArchived());
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new UnarchiveCommand(outOfBoundIndex), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        model = new ModelManager(new AddressBookBuilder().withPerson(ALICE.withArchived(true))
                .withPerson(BENSON.withArchived(true)).build(), new UserPrefs());
        new ListArchiveCommand().execute(model);
        model.updateFilteredPersonList(ALICE::isSamePerson);
        assertTrue(INDEX_SECOND_PERSON.getZeroBased() < model.getAddressBook().getArchivedPersonList().size());

        assertCommandFailure(new UnarchiveCommand(INDEX_SECOND_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyList_throwsCommandException() {
        model = new ModelManager();

        assertCommandFailure(new UnarchiveCommand(INDEX_FIRST_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_personAlreadyUnarchived_throwsCommandException() {
        new ListCommand().execute(model);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());

        assertCommandFailure(new UnarchiveCommand(INDEX_FIRST_PERSON), model,
                UnarchiveCommand.MESSAGE_ALREADY_UNARCHIVED);
    }
}
