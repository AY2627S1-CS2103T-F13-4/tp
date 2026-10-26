package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Lesson;
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
    public void archivePerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.archivePerson(null));
    }

    @Test
    public void unarchivePerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.unarchivePerson(null));
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
        assertEquals(List.of(BENSON), modelManager.getAddressBook().getPersonList());
        Student archivedPerson = modelManager.getAddressBook().getArchivedPersonList().get(0);
        assertNotSame(ALICE, archivedPerson);
        assertTrue(archivedPerson.isSamePerson(ALICE));
        assertTrue(archivedPerson.isArchived());
        assertFalse(ALICE.isArchived());
    }

    @Test
    public void unarchivePerson_archiveView_notifiesBothListsAndKeepsArchiveView() {
        Student archivedAlice = ALICE.withArchived(true);
        Student archivedBenson = BENSON.withArchived(true);
        modelManager = new ModelManager(new AddressBookBuilder().withPerson(archivedAlice).withPerson(archivedBenson)
                .build(), new UserPrefs());
        modelManager.setArchiveView(true);
        ObservableList<Student> displayedPersons = modelManager.getFilteredPersonList();
        List<Student> removedPersons = new ArrayList<>();
        List<Student> addedPersons = new ArrayList<>();
        displayedPersons.addListener((ListChangeListener<Student>) change -> {
            while (change.next()) {
                removedPersons.addAll(change.getRemoved());
            }
        });
        modelManager.getFilteredActivePersonList().addListener((ListChangeListener<Student>) change -> {
            while (change.next()) {
                addedPersons.addAll(change.getAddedSubList());
            }
        });

        modelManager.unarchivePerson(archivedAlice);

        assertEquals(List.of(archivedBenson), displayedPersons);
        assertEquals(List.of(archivedAlice), removedPersons);
        assertEquals(List.of(ALICE), addedPersons);
        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
        assertEquals(List.of(archivedBenson), modelManager.getAddressBook().getArchivedPersonList());
        assertTrue(modelManager.isArchiveView());
        assertTrue(archivedAlice.isArchived());
    }

    @Test
    public void constructor_archivedStudents_keepsSeparateObservableLists() {
        Student archivedBenson = BENSON.withArchived(true);
        modelManager = new ModelManager(new AddressBookBuilder().withPerson(ALICE).withPerson(archivedBenson)
                .build(), new UserPrefs());

        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
        assertSame(modelManager.getFilteredActivePersonList(), modelManager.getFilteredPersonList());
        assertEquals(List.of(archivedBenson), modelManager.getFilteredArchivedPersonList());
        modelManager.setArchiveView(true);
        assertSame(modelManager.getFilteredArchivedPersonList(), modelManager.getFilteredPersonList());
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void updateFilteredPersonList_switchViews_retainsIndependentStudentAndLessonFilters() {
        Student archivedBenson = BENSON.withArchived(true);
        modelManager = new ModelManager(new AddressBookBuilder().withPerson(ALICE).withPerson(archivedBenson)
                .build(), new UserPrefs());
        modelManager.updateFilteredPersonList(student -> false, lesson -> false);
        Predicate<Lesson> activeLessonPredicate = modelManager.getLessonPredicate().getValue();

        modelManager.setArchiveView(true);
        assertEquals(List.of(archivedBenson), modelManager.getFilteredPersonList());
        modelManager.updateFilteredPersonList(student -> false);
        assertTrue(modelManager.getFilteredPersonList().isEmpty());
        assertSame(activeLessonPredicate, modelManager.getLessonPredicate().getValue());

        modelManager.setArchiveView(false);
        assertTrue(modelManager.getFilteredPersonList().isEmpty());
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
        assertTrue(modelManager.getFilteredArchivedPersonList().isEmpty());
    }

    @Test
    public void getFilteredArchivedPersonList_groupedByStageInArchivedOrder() {
        Student jc = new PersonBuilder(ALICE).withSchoolingLevel(new SchoolingLevel("JC1")).build().withArchived(true);
        Student primary = new PersonBuilder(BENSON).withSchoolingLevel(new SchoolingLevel("P5"))
                .build().withArchived(true);
        modelManager = new ModelManager(new AddressBookBuilder().withPerson(jc).withPerson(primary)
                .build(), new UserPrefs());

        assertEquals(List.of(primary, jc), modelManager.getFilteredArchivedPersonList());
        Student edited = new PersonBuilder(primary).withPhone("99999999").build();
        modelManager.setPerson(primary, edited);
        assertEquals(List.of(edited, jc), modelManager.getFilteredArchivedPersonList());
        assertTrue(modelManager.getFilteredActivePersonList().isEmpty());
    }

    @Test
    public void setAddressBook_replacement_keepsBothObservableViewsAndSelectedList() {
        modelManager = new ModelManager(new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON.withArchived(true))
                .build(), new UserPrefs());
        ObservableList<Student> activePersons = modelManager.getFilteredActivePersonList();
        ObservableList<Student> archivedPersons = modelManager.getFilteredArchivedPersonList();
        modelManager.setArchiveView(true);

        modelManager.setAddressBook(new AddressBookBuilder().withPerson(BENSON).withPerson(ALICE.withArchived(true))
                .build());

        assertEquals(List.of(BENSON), activePersons);
        assertEquals(List.of(ALICE.withArchived(true)), archivedPersons);
        assertSame(activePersons, modelManager.getFilteredActivePersonList());
        assertSame(archivedPersons, modelManager.getFilteredPersonList());
        assertTrue(modelManager.isArchiveView());
    }

    // Students are listed by school stage, then level, then name, so the table can show Primary, Secondary and JC
    // groups, and the numbers on screen must stay the command indices of the shown order.
    @Test
    public void getFilteredPersonList_sortedByStageThenLevelThenName() {
        Student jc = new PersonBuilder().withName("Jay").withSchoolingLevel(new SchoolingLevel("JC1")).build();
        Student pri1 = new PersonBuilder().withName("Pat").withSchoolingLevel(new SchoolingLevel("P5")).build();
        Student noLevel = new PersonBuilder().withName("Nia").build();
        Student pri2 = new PersonBuilder().withName("Pia").withSchoolingLevel(new SchoolingLevel("P6")).build();
        Student pri3 = new PersonBuilder().withName("Poh").withSchoolingLevel(new SchoolingLevel("P4")).build();
        Student pri4 = new PersonBuilder().withName("Amy").withSchoolingLevel(new SchoolingLevel("Primary 5")).build();
        Student otherB = new PersonBuilder().withName("Bob").withSchoolingLevel(new SchoolingLevel("Poly Y1")).build();
        Student otherA = new PersonBuilder().withName("Zed").withSchoolingLevel(new SchoolingLevel("IB")).build();
        modelManager = new ModelManager(new AddressBookBuilder().withPerson(jc).withPerson(pri1).withPerson(noLevel)
                .withPerson(pri2).withPerson(pri3).withPerson(pri4).withPerson(otherB)
                .withPerson(otherA).build(), new UserPrefs());
        // P5 and "Primary 5" are one level, so Amy comes before Pat. Unrecognised levels follow JC by level text.
        assertEquals(List.of(pri3, pri4, pri1, pri2, jc, otherA, otherB, noLevel),
                modelManager.getFilteredPersonList());

        // Editing a student without changing their level or name keeps their place.
        Student editedPri1 = new PersonBuilder(pri1).withPhone("99999999").build();
        modelManager.setPerson(pri1, editedPri1);
        assertEquals(List.of(pri3, pri4, editedPri1, pri2, jc, otherA, otherB, noLevel),
                modelManager.getFilteredPersonList());

        // Moving a student to another stage and back returns it to its old place.
        Student secPri2 = new PersonBuilder(pri2).withSchoolingLevel(new SchoolingLevel("Sec 1")).build();
        modelManager.setPerson(pri2, secPri2);
        assertEquals(List.of(pri3, pri4, editedPri1, secPri2, jc, otherA, otherB, noLevel),
                modelManager.getFilteredPersonList());
        modelManager.setPerson(secPri2, pri2);
        assertEquals(List.of(pri3, pri4, editedPri1, pri2, jc, otherA, otherB, noLevel),
                modelManager.getFilteredPersonList());
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

        modelManager.setArchiveView(true);
        assertFalse(modelManager.equals(modelManagerCopy));
        modelManager.setArchiveView(false);

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
