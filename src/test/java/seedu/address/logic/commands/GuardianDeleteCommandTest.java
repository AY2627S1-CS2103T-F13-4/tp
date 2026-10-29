package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.School;
import seedu.address.model.person.Student;
import seedu.address.testutil.PersonBuilder;

public class GuardianDeleteCommandTest {
    private static final Guardian CONTACT = new Guardian(new Name("Janet Tan"), new Phone("91234567"));
    private final Student original = new PersonBuilder(ALICE).withSchool(new School("Nanyang Primary School")).build();
    private final Model model = new ModelManager();

    @BeforeEach
    public void setUp() {
        model.addPerson(original);
    }

    @Test
    public void constructor_invalidArguments_failure() {
        assertThrows(NullPointerException.class, () -> new GuardianDeleteCommand(null));
    }

    @Test
    public void execute_preservesSchool() {
        Student student = new PersonBuilder(original).withGuardian(CONTACT).build();
        model.setPerson(original, student);
        Model expectedModel = new ModelManager();
        expectedModel.addPerson(original);
        assertCommandSuccess(new GuardianDeleteCommand(INDEX_FIRST_PERSON), model,
                "Guardian deleted for " + original.getName(), expectedModel);
    }

    @Test
    public void execute_missingGuardian_failure() {
        assertCommandFailure(new GuardianDeleteCommand(INDEX_FIRST_PERSON), model,
                GuardianDeleteCommand.MESSAGE_NOT_FOUND);
    }

    @Test
    public void execute_invalidStudentIndex_failure() {
        assertCommandFailure(new GuardianDeleteCommand(INDEX_SECOND_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_archivedStudent_preservesArchiveStatus() throws CommandException {
        Student archived = new PersonBuilder(original).withGuardian(CONTACT).build()
                .withArchived(true);
        model.setPerson(original, archived);
        new ListArchiveCommand().execute(model);
        model.updateFilteredPersonList(archived::isSamePerson);

        new GuardianDeleteCommand(INDEX_FIRST_PERSON).execute(model);

        Student updated = model.getFilteredPersonList().getFirst();
        assertEquals(new PersonBuilder(archived).withGuardian(null).build(), updated);
        assertTrue(updated.isArchived());
    }

    @Test
    public void equals() {
        GuardianDeleteCommand command = new GuardianDeleteCommand(INDEX_FIRST_PERSON);
        assertEquals(command, new GuardianDeleteCommand(INDEX_FIRST_PERSON));
        assertEquals(command.hashCode(), new GuardianDeleteCommand(INDEX_FIRST_PERSON).hashCode());
        assertNotEquals(command, new GuardianDeleteCommand(INDEX_SECOND_PERSON));
        assertNotEquals(command, null);
    }

}
