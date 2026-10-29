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
import seedu.address.logic.commands.GuardianEditCommand.EditGuardianDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.School;
import seedu.address.model.person.Student;
import seedu.address.testutil.EditGuardianDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

public class GuardianEditCommandTest {
    private static final Guardian CONTACT = new Guardian(new Name("Janet Tan"), new Phone("91234567"));
    private final Student original = new PersonBuilder(ALICE).withSchool(new School("Nanyang Primary School")).build();
    private final Model model = new ModelManager();

    @BeforeEach
    public void setUp() {
        model.addPerson(original);
    }

    @Test
    public void constructor_invalidArguments_failure() {
        assertThrows(NullPointerException.class, () -> new GuardianEditCommand(null,
                new EditGuardianDescriptorBuilder().withPhone(CONTACT.getPhone()).build()));
        assertThrows(NullPointerException.class, () -> new GuardianEditCommand(INDEX_FIRST_PERSON, null));
        assertThrows(IllegalArgumentException.class, () -> new GuardianEditCommand(INDEX_FIRST_PERSON,
                new EditGuardianDescriptor()));
    }

    @Test
    public void execute_preservesSchool() {
        Student student = new PersonBuilder(original).withGuardian(CONTACT).build();
        model.setPerson(original, student);
        Phone updatedPhone = new Phone("98765432");
        Student expectedStudent = new PersonBuilder(student)
                .withGuardian(new Guardian(CONTACT.getName(), updatedPhone)).build();
        Model expectedModel = new ModelManager();
        expectedModel.addPerson(expectedStudent);
        GuardianEditCommand command = new GuardianEditCommand(INDEX_FIRST_PERSON,
                new EditGuardianDescriptorBuilder().withPhone(updatedPhone).build());
        assertCommandSuccess(command, model, "Guardian edited for " + original.getName(), expectedModel);
    }

    @Test
    public void execute_missingGuardian_failure() {
        assertCommandFailure(new GuardianEditCommand(INDEX_FIRST_PERSON,
                new EditGuardianDescriptorBuilder().withPhone(CONTACT.getPhone()).build()), model,
                GuardianEditCommand.MESSAGE_NOT_FOUND);
    }

    @Test
    public void constructor_descriptor_isDefensivelyCopied() {
        EditGuardianDescriptor descriptor = new EditGuardianDescriptorBuilder().withPhone(CONTACT.getPhone()).build();
        GuardianEditCommand command = new GuardianEditCommand(INDEX_FIRST_PERSON, descriptor);
        GuardianEditCommand expected = new GuardianEditCommand(INDEX_FIRST_PERSON,
                new EditGuardianDescriptor(descriptor));
        descriptor.setName(new Name("Someone Else"));
        assertEquals(expected, command);
    }

    @Test
    public void execute_invalidStudentIndex_failure() {
        assertCommandFailure(new GuardianEditCommand(INDEX_SECOND_PERSON,
                new EditGuardianDescriptorBuilder().withPhone(CONTACT.getPhone()).build()), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_archivedStudent_preservesArchiveStatus() throws CommandException {
        Student archived = new PersonBuilder(original).withGuardian(CONTACT).build()
                .withArchived(true);
        model.setPerson(original, archived);
        new ListArchiveCommand().execute(model);
        model.updateFilteredPersonList(archived::isSamePerson);

        Phone updatedPhone = new Phone("98765432");
        new GuardianEditCommand(INDEX_FIRST_PERSON,
                new EditGuardianDescriptorBuilder().withPhone(updatedPhone).build()).execute(model);

        Student updated = model.getFilteredPersonList().getFirst();
        assertEquals(new PersonBuilder(archived)
                .withGuardian(new Guardian(CONTACT.getName(), updatedPhone)).build(), updated);
        assertTrue(updated.isArchived());
    }

    @Test
    public void equals() {
        EditGuardianDescriptor descriptor = new EditGuardianDescriptorBuilder().withPhone(CONTACT.getPhone()).build();
        GuardianEditCommand command = new GuardianEditCommand(INDEX_FIRST_PERSON, descriptor);
        assertEquals(command, new GuardianEditCommand(INDEX_FIRST_PERSON, new EditGuardianDescriptor(descriptor)));
        assertEquals(command.hashCode(), new GuardianEditCommand(INDEX_FIRST_PERSON,
                new EditGuardianDescriptor(descriptor)).hashCode());
        assertNotEquals(command, new GuardianEditCommand(INDEX_SECOND_PERSON, descriptor));
        assertNotEquals(command, null);
    }

}
