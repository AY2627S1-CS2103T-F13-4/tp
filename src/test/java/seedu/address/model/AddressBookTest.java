package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Collection;
import java.util.List;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.collections.ListChangeListener;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.Student;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.testutil.PersonBuilder;

public class AddressBookTest {

    private final AddressBook addressBook = new AddressBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), addressBook.getPersonList());
        assertEquals(List.of(), addressBook.getArchivedPersonList());
    }

    @Test
    public void constructor_withActiveAndArchivedPersons_copiesBothLists() {
        addressBook.setPersons(List.of(ALICE, BENSON.withArchived(true)));

        AddressBook copy = new AddressBook(addressBook);

        assertEquals(addressBook, copy);
        assertEquals(addressBook.hashCode(), copy.hashCode());
        addressBook.removePerson(BENSON.withArchived(true));
        assertNotEquals(addressBook, copy);
        assertEquals(List.of(BENSON.withArchived(true)), copy.getArchivedPersonList());
    }

    @Test
    public void setPersons_mixedArchiveStatus_replacesBothLists() {
        addressBook.setPersons(List.of(ALICE.withArchived(true), BENSON));
        assertEquals(List.of(BENSON), addressBook.getPersonList());
        assertEquals(List.of(ALICE.withArchived(true)), addressBook.getArchivedPersonList());

        addressBook.setPersons(List.of(ALICE));
        assertEquals(List.of(ALICE), addressBook.getPersonList());
        assertTrue(addressBook.getArchivedPersonList().isEmpty());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyAddressBook_replacesData() {
        AddressBook newData = getTypicalAddressBook();
        newData.archivePerson(ALICE);
        addressBook.resetData(newData);
        assertEquals(newData, addressBook);
    }

    @Test
    public void resetData_sameAddressBook_preservesBothLists() {
        addressBook.setPersons(List.of(ALICE, BENSON.withArchived(true)));
        AddressBook expected = new AddressBook(addressBook);

        addressBook.resetData(addressBook);

        assertEquals(expected, addressBook);
    }

    @Test
    public void resetData_duplicateAcrossLists_throwsWithoutChangingData() {
        addressBook.setPersons(List.of(BENSON, ALICE.withArchived(true)));
        AddressBook expected = new AddressBook(addressBook);
        AddressBookStub newData = new AddressBookStub(List.of(ALICE), List.of(ALICE.withArchived(true)));

        assertThrows(DuplicatePersonException.class, () -> addressBook.resetData(newData));
        assertEquals(expected, addressBook);
    }

    @Test
    public void resetData_withDuplicatePersons_throwsDuplicatePersonException() {
        // Two persons with the same identity fields
        Student editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Student> newPersons = List.of(ALICE, editedAlice);
        AddressBookStub newData = new AddressBookStub(newPersons);

        assertThrows(DuplicatePersonException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        assertTrue(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personWithSameIdentityFieldsInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        Student editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(addressBook.hasPerson(editedAlice));
    }

    @Test
    public void addPerson_archivedPerson_routesToArchivedListAndPreventsActiveDuplicate() {
        addressBook.addPerson(ALICE.withArchived(true));

        assertTrue(addressBook.getPersonList().isEmpty());
        assertEquals(List.of(ALICE.withArchived(true)), addressBook.getArchivedPersonList());
        assertTrue(addressBook.hasPerson(ALICE));
        assertThrows(DuplicatePersonException.class, () -> addressBook.addPerson(ALICE));
    }

    @Test
    public void addPerson_activePerson_preventsArchivedDuplicate() {
        addressBook.addPerson(ALICE);

        assertThrows(DuplicatePersonException.class, () -> addressBook.addPerson(ALICE.withArchived(true)));
    }

    @Test
    public void setPerson_archivedPerson_updatesArchivedList() {
        Student archivedAlice = ALICE.withArchived(true);
        Student editedAlice = new PersonBuilder(archivedAlice).withAddress(VALID_ADDRESS_BOB).build();
        addressBook.addPerson(archivedAlice);

        addressBook.setPerson(archivedAlice, editedAlice);

        assertTrue(addressBook.getPersonList().isEmpty());
        assertEquals(List.of(editedAlice), addressBook.getArchivedPersonList());
    }

    @Test
    public void setPerson_duplicateInOtherList_throwsWithoutChangingData() {
        Student archivedBenson = BENSON.withArchived(true);
        addressBook.setPersons(List.of(ALICE, archivedBenson));
        AddressBook expected = new AddressBook(addressBook);

        assertThrows(DuplicatePersonException.class, () -> addressBook.setPerson(ALICE, BENSON));
        assertThrows(DuplicatePersonException.class, () -> addressBook.setPerson(
                archivedBenson, ALICE.withArchived(true)));
        assertThrows(DuplicatePersonException.class, () -> addressBook.setPerson(ALICE, archivedBenson));
        assertEquals(expected, addressBook);
    }

    @Test
    public void removePerson_archivedPerson_removesFromArchivedList() {
        Student archivedAlice = ALICE.withArchived(true);
        addressBook.setPersons(List.of(archivedAlice, BENSON));

        addressBook.removePerson(archivedAlice);

        assertEquals(List.of(BENSON), addressBook.getPersonList());
        assertTrue(addressBook.getArchivedPersonList().isEmpty());
        assertFalse(addressBook.hasPerson(ALICE));
    }

    @Test
    public void archivePerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.archivePerson(null));
    }

    @Test
    public void archivePerson_personNotInAddressBook_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> addressBook.archivePerson(ALICE));
    }

    @Test
    public void archivePerson_existingPerson_movesToArchivedList() {
        addressBook.setPersons(List.of(ALICE, BENSON));
        ObservableList<Student> activePersons = addressBook.getPersonList();
        ObservableList<Student> archivedPersons = addressBook.getArchivedPersonList();

        addressBook.archivePerson(ALICE);

        assertEquals(List.of(BENSON), activePersons);
        assertEquals(List.of(ALICE.withArchived(true)), archivedPersons);
        assertFalse(ALICE.isArchived());
    }

    @Test
    public void unarchivePerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.unarchivePerson(null));
    }

    @Test
    public void unarchivePerson_personNotInAddressBook_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> addressBook.unarchivePerson(ALICE.withArchived(true)));
    }

    @Test
    public void unarchivePerson_existingPerson_movesToActiveList() {
        Student archivedAlice = ALICE.withArchived(true);
        addressBook.setPersons(List.of(BENSON, archivedAlice));

        addressBook.unarchivePerson(archivedAlice);

        assertEquals(List.of(BENSON, ALICE), addressBook.getPersonList());
        assertTrue(addressBook.getArchivedPersonList().isEmpty());
        assertTrue(archivedAlice.isArchived());
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getPersonList().remove(0));
    }

    @Test
    public void getArchivedPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getArchivedPersonList().remove(0));
    }

    @Test
    public void toStringMethod() {
        String expected = AddressBook.class.getCanonicalName() + "{persons=" + addressBook.getPersonList()
                + ", archivedPersons=" + addressBook.getArchivedPersonList() + "}";
        assertEquals(expected, addressBook.toString());
    }

    /**
     * A stub ReadOnlyAddressBook whose persons list can violate interface constraints.
     */
    private static class AddressBookStub implements ReadOnlyAddressBook {
        private final ObservableList<Student> persons = FXCollections.observableArrayList();
        private final ObservableList<Student> archivedPersons = FXCollections.observableArrayList();
        private final SimpleIntegerProperty studentCount = new SimpleIntegerProperty(0);

        AddressBookStub(Collection<Student> persons) {
            this(persons, List.of());
        }

        AddressBookStub(Collection<Student> persons, Collection<Student> archivedPersons) {
            ListChangeListener<? super Student> listener = change -> studentCount.set(persons.size() + archivedPersons.size());
            this.persons.addListener(listener);
            this.archivedPersons.addListener(listener);

            this.persons.setAll(persons);
            this.archivedPersons.setAll(archivedPersons);
        }

        @Override
        public ObservableIntegerValue getStudentCount() { return studentCount; }

        @Override
        public ObservableList<Student> getPersonList() {
            return persons;
        }

        @Override
        public ObservableList<Student> getArchivedPersonList() {
            return archivedPersons;
        }
    }

}
