package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Student;
import seedu.address.model.person.UniquePersonList;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison).
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();
    private final UniquePersonList archivedPersons = new UniquePersonList();
    private final SimpleIntegerProperty size = new SimpleIntegerProperty(0);
    private final SimpleIntegerProperty lessonCount = new SimpleIntegerProperty(0);

    public AddressBook() {
        persons.getInternalSize().addListener(this::updateStudentCount);
        persons.getLessonCount().addListener(this::updateLessonCount);
        archivedPersons.getInternalSize().addListener(this::updateStudentCount);
        archivedPersons.getLessonCount().addListener(this::updateLessonCount);
    }

    /**
     * Creates an AddressBook using the Persons in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces both person lists with {@code persons}, separated by archive status.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Student> persons) {
        requireNonNull(persons);
        AddressBook replacement = new AddressBook();
        persons.forEach(replacement::addPerson);

        this.persons.setPersons(replacement.persons);
        archivedPersons.setPersons(replacement.archivedPersons);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        List<Student> replacement = new ArrayList<>(newData.getPersonList());
        replacement.addAll(newData.getArchivedPersonList());
        setPersons(replacement);
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Student person) {
        requireNonNull(person);
        return persons.contains(person) || archivedPersons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Student p) {
        requireNonNull(p);
        if (hasPerson(p)) {
            throw new DuplicatePersonException();
        }
        getListFor(p).add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Student target, Student editedPerson) {
        requireAllNonNull(target, editedPerson);

        UniquePersonList source = getListFor(target);
        UniquePersonList destination = getListFor(editedPerson);
        if (!source.asUnmodifiableObservableList().contains(target)) {
            throw new PersonNotFoundException();
        }
        if (!target.isSamePerson(editedPerson) && hasPerson(editedPerson)) {
            throw new DuplicatePersonException();
        }

        if (source == destination) {
            source.setPerson(target, editedPerson);
        } else {
            source.remove(target);
            destination.add(editedPerson);
        }
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Student key) {
        requireNonNull(key);
        getListFor(key).remove(key);
    }

    /**
     * Archives {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void archivePerson(Student key) {
        requireNonNull(key);
        setPerson(key, key.withArchived(true));
    }

    /**
     * Unarchives {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void unarchivePerson(Student key) {
        requireNonNull(key);
        setPerson(key, key.withArchived(false));
    }

    /** Returns the list that stores the given person's archive status. */
    private UniquePersonList getListFor(Student person) {
        return person.isArchived() ? archivedPersons : persons;
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .add("archivedPersons", archivedPersons)
                .toString();
    }

    @Override
    public ObservableIntegerValue getActiveStudentCount() {
        return persons.getInternalSize();
    }

    @Override
    public ObservableIntegerValue getArchivedStudentCount() {
        return archivedPersons.getInternalSize();
    }

    private void updateStudentCount(Observable change) {
        size.set(persons.getInternalSize().intValue() + archivedPersons.getInternalSize().intValue());
    }

    @Override
    public ObservableIntegerValue getLessonCount() {
        return lessonCount;
    }

    private void updateLessonCount(Observable change) {
        lessonCount.set(persons.getLessonCount().intValue() + archivedPersons.getInternalSize().intValue());
    }

    @Override
    public ObservableList<Student> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<Student> getArchivedPersonList() {
        return archivedPersons.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return persons.equals(otherAddressBook.persons)
                && archivedPersons.equals(otherAddressBook.archivedPersons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, archivedPersons);
    }
}
