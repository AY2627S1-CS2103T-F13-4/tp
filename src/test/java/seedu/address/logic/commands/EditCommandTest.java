package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.GuardianContact;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private static final WeeklyLessonSlot MONDAY_SLOT = new WeeklyLessonSlot(DayOfWeek.MONDAY,
            LocalTime.of(16, 0), LocalTime.of(17, 30));
    private static final WeeklyLessonSlot TUESDAY_SLOT = new WeeklyLessonSlot(DayOfWeek.TUESDAY,
            LocalTime.of(10, 0), LocalTime.of(11, 0));

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person editedPerson = new PersonBuilder().withWeeklyLessonSlot(MONDAY_SLOT).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).withTags(VALID_TAG_HUSBAND).build();
        EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }


    @Test
    public void execute_setThenClearSchoolingLevel_success() {
        Person first = model.getFilteredPersonList().get(0);

        EditPersonDescriptor set = new EditPersonDescriptorBuilder().withSchoolingLevel("Primary 5").build();
        Person withLevel = new PersonBuilder(first)
                .withSchoolingLevel(new SchoolingLevel("Primary 5")).build();
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(first, withLevel);
        assertCommandSuccess(new EditCommand(INDEX_FIRST_PERSON, set), model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(withLevel)), expectedModel);

        // editing another field preserves the level
        Person renamed = new PersonBuilder(withLevel).withPhone(VALID_PHONE_BOB).build();
        EditPersonDescriptor phoneOnly = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();
        Model expectedModel2 = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel2.setPerson(withLevel, renamed);
        assertCommandSuccess(new EditCommand(INDEX_FIRST_PERSON, phoneOnly), model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(renamed)), expectedModel2);

        // clearing removes it
        EditPersonDescriptor clear = new EditPersonDescriptorBuilder().withSchoolingLevel(null).build();
        Person cleared = new PersonBuilder(renamed).withSchoolingLevel(null).build();
        Model expectedModel3 = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel3.setPerson(renamed, cleared);
        assertCommandSuccess(new EditCommand(INDEX_FIRST_PERSON, clear), model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(cleared)), expectedModel3);
    }

    @Test
    public void execute_setWeeklyLessonSlot_preservesOtherFields() {
        Person original = personWithTuitionDetails().withWeeklyLessonSlot(null).build();
        Person editedPerson = new PersonBuilder(original).withWeeklyLessonSlot(MONDAY_SLOT).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withWeeklyLessonSlot(MONDAY_SLOT).build();

        assertSuccessfulEdit(original, descriptor, editedPerson);
    }

    @Test
    public void execute_replaceWeeklyLessonSlot_preservesOtherFields() {
        Person original = personWithTuitionDetails().withWeeklyLessonSlot(MONDAY_SLOT).build();
        Person editedPerson = new PersonBuilder(original).withWeeklyLessonSlot(TUESDAY_SLOT).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withWeeklyLessonSlot(TUESDAY_SLOT).build();

        assertSuccessfulEdit(original, descriptor, editedPerson);
    }

    @Test
    public void execute_clearWeeklyLessonSlot_preservesOtherFields() {
        Person original = personWithTuitionDetails().withWeeklyLessonSlot(MONDAY_SLOT).build();
        Person editedPerson = new PersonBuilder(original).withWeeklyLessonSlot(null).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withWeeklyLessonSlot(null).build();

        assertSuccessfulEdit(original, descriptor, editedPerson);
    }

    @Test
    public void execute_editPhone_preservesWeeklyLessonSlotAndOtherTuitionFields() {
        Person original = personWithTuitionDetails().withWeeklyLessonSlot(MONDAY_SLOT).build();
        Person editedPerson = new PersonBuilder(original).withPhone(VALID_PHONE_BOB).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();

        assertSuccessfulEdit(original, descriptor, editedPerson);
    }

    private PersonBuilder personWithTuitionDetails() {
        return new PersonBuilder(model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()))
                .withSubject(new Subject("Mathematics"))
                .withSchoolingLevel(new SchoolingLevel("Primary 5"))
                .withGuardianContact(new GuardianContact(new Name("Janet Tan"), new Phone("91234567")))
                .withHourlyRate(new HourlyRate(new BigDecimal("45.50")));
    }

    private void assertSuccessfulEdit(Person original, EditPersonDescriptor descriptor, Person editedPerson) {
        model.setPerson(model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()), original);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, editedPerson);

        assertCommandSuccess(new EditCommand(INDEX_FIRST_PERSON, descriptor), model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)), expectedModel);
    }
}
