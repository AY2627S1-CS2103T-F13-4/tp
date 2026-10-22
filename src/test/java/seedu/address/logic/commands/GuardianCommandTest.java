package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.GuardianCommand.Operation;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Student;
import seedu.address.testutil.PersonBuilder;

public class GuardianCommandTest {

    @Test
    public void execute_editArchivedStudentGuardian_preservesArchiveStatus() throws CommandException {
        Guardian originalGuardian = new Guardian(new Name("Janet Tan"), new Phone("91234567"));
        Student archived = new PersonBuilder().withGuardian(originalGuardian).build().withArchived(true);
        Model model = new ModelManager();
        model.addPerson(archived);
        model.updateFilteredPersonList(archived::isSamePerson);
        Phone updatedPhone = new Phone("98765432");
        GuardianCommand command = new GuardianCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.empty(), Optional.of(updatedPhone));

        command.execute(model);

        Student edited = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Student expected = new PersonBuilder(archived)
                .withGuardian(new Guardian(originalGuardian.getName(), updatedPhone)).build();
        assertEquals(expected, edited);
        assertTrue(edited.isArchived());
    }
}
