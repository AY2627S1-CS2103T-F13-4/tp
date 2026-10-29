package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.VALID_SCHOOL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_SCHOOL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.School;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        Student validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                expectedModel);
    }

    @Test
    public void execute_newPersonWithSchool_success() throws Exception {
        Student validPerson = new PersonBuilder().withTags("friend").withSchool(new School(VALID_SCHOOL_AMY))
                .withSchoolingLevel(new SchoolingLevel("Primary 5")).build();
        Command command = new AddressBookParser().parseCommand(PersonUtil.getAddCommand(validPerson));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(command, model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)), expectedModel);
    }

    @Test
    public void execute_duplicatePersonWithDifferentSchool_throwsCommandException() {
        Student personInList = model.getAddressBook().getPersonList().get(0);
        Student differentSchool = new PersonBuilder(personInList).withSchool(new School(VALID_SCHOOL_BOB)).build();

        assertCommandFailure(new AddCommand(differentSchool), model, AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_duplicatePersonWithExtraSpacesInName_throwsCommandException() throws Exception {
        Student personInList = model.getAddressBook().getPersonList().get(0);
        String spacedName = personInList.getName().fullName.replace(" ", "   ");
        String userInput = PersonUtil.getAddCommand(personInList)
                .replace(PREFIX_NAME + personInList.getName().fullName, PREFIX_NAME + spacedName);
        Command command = new AddressBookParser().parseCommand(userInput);

        assertCommandFailure(command, model, AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Student personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

}
