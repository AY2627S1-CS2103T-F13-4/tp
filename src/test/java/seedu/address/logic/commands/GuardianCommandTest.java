package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.GuardianCommand.Operation;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.School;
import seedu.address.model.person.Student;
import seedu.address.testutil.PersonBuilder;

public class GuardianCommandTest {

    @Test
    public void execute_guardianChanges_preserveSchool() {
        Student student = new PersonBuilder(ALICE).withSchool(new School("Nanyang Primary School")).build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(student);
        Model model = new ModelManager(addressBook, new UserPrefs());
        Model expectedModel = new ModelManager(addressBook, new UserPrefs());
        Guardian guardian = new Guardian(new Name("Janet Tan"), new Phone("91234567"));
        Student withGuardian = new PersonBuilder(student).withGuardian(guardian).build();
        expectedModel.setPerson(student, withGuardian);

        GuardianCommand add = new GuardianCommand(INDEX_FIRST_PERSON, Operation.ADD,
                Optional.of(guardian.getName()), Optional.of(guardian.getPhone()));
        assertCommandSuccess(add, model, "Guardian added for " + student.getName() + ".", expectedModel);

        Guardian editedGuardian = new Guardian(guardian.getName(), new Phone("98765432"));
        Student withEditedGuardian = new PersonBuilder(student).withGuardian(editedGuardian).build();
        expectedModel.setPerson(withGuardian, withEditedGuardian);
        GuardianCommand edit = new GuardianCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.empty(), Optional.of(editedGuardian.getPhone()));
        assertCommandSuccess(edit, model, "Guardian updated for " + student.getName() + ".", expectedModel);

        expectedModel.setPerson(withEditedGuardian, student);
        GuardianCommand delete = new GuardianCommand(INDEX_FIRST_PERSON, Operation.DELETE,
                Optional.empty(), Optional.empty());
        assertCommandSuccess(delete, model, "Guardian removed for " + student.getName() + ".", expectedModel);
    }
}
