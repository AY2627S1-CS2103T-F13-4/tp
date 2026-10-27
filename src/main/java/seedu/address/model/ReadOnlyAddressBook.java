package seedu.address.model;

import javafx.beans.value.ObservableIntegerValue;
import javafx.collections.ObservableList;
import seedu.address.model.person.Student;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {
    /**
     * Returns an unmodifiable view of the number of selected active students.
     */
    ObservableIntegerValue getActiveStudentCount();

    /**
     * Returns an unmodifiable view of the number of selected archived students.
     */
    ObservableIntegerValue getArchivedStudentCount();

    /**
     * Returns an unmodifiable view of the number of lessons.
     */
    ObservableIntegerValue getLessonCount();

    /**
     * Returns an unmodifiable view of the active persons list.
     * Persons are unique across both active and archived lists.
     */
    ObservableList<Student> getPersonList();

    /**
     * Returns an unmodifiable view of the archived persons list.
     * Persons are unique across both active and archived lists.
     */
    ObservableList<Student> getArchivedPersonList();

}
