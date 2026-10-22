package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void archivePerson_showAllPredicateInstalled_notifiesDisplayedListAndRetainsArchivedPerson() {
        modelManager.addPerson(ALICE);
        modelManager.addPerson(BENSON);
        ObservableList<Student> displayedPersons = modelManager.getFilteredPersonList();
        List<Student> removedPersons = new ArrayList<>();
        displayedPersons.addListener((ListChangeListener<Student>) change -> {
            while (change.next()) {
                removedPersons.addAll(change.getRemoved());
            }
        });

        modelManager.archivePerson(ALICE);

        assertEquals(List.of(BENSON), displayedPersons);
        assertEquals(List.of(ALICE), removedPersons);
        assertEquals(2, modelManager.getAddressBook().getPersonList().size());
        Student archivedPerson = modelManager.getAddressBook().getPersonList().get(0);
        assertNotSame(ALICE, archivedPerson);
        assertTrue(archivedPerson.isSamePerson(ALICE));
        assertTrue(archivedPerson.isArchived());
        assertFalse(ALICE.isArchived());
    }

    // Students are listed by school stage so the table can show Primary, Secondary and JC groups, and the numbers on
    // screen must stay the command indices: an edit must never move a student to another place in its group.
    @Test
    public void getFilteredPersonList_groupedByStageInAddedOrder() {
        Student jc = new PersonBuilder().withName("Jay").withSchoolingLevel(new SchoolingLevel("JC1")).build();
        Student pri1 = new PersonBuilder().withName("Pat").withSchoolingLevel(new SchoolingLevel("P5")).build();
        Student noLevel = new PersonBuilder().withName("Nia").build();
        Student pri2 = new PersonBuilder().withName("Pia").withSchoolingLevel(new SchoolingLevel("P6")).build();
        Student pri3 = new PersonBuilder().withName("Poh").withSchoolingLevel(new SchoolingLevel("P4")).build();
        modelManager = new ModelManager(new AddressBookBuilder().withPerson(jc).withPerson(pri1).withPerson(noLevel)
                .withPerson(pri2).withPerson(pri3).build(), new UserPrefs());
        assertEquals(List.of(pri1, pri2, pri3, jc, noLevel), modelManager.getFilteredPersonList());

        // Editing the first of a group keeps it first.
        Student editedPri1 = new PersonBuilder(pri1).withPhone("99999999").build();
        modelManager.setPerson(pri1, editedPri1);
        assertEquals(List.of(editedPri1, pri2, pri3, jc, noLevel), modelManager.getFilteredPersonList());

        // Moving a student to another group and back returns it to its old place.
        Student secPri2 = new PersonBuilder(pri2).withSchoolingLevel(new SchoolingLevel("Sec 1")).build();
        modelManager.setPerson(pri2, secPri2);
        assertEquals(List.of(editedPri1, pri3, secPri2, jc, noLevel), modelManager.getFilteredPersonList());
        modelManager.setPerson(secPri2, pri2);
        assertEquals(List.of(editedPri1, pri2, pri3, jc, noLevel), modelManager.getFilteredPersonList());
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }
}
