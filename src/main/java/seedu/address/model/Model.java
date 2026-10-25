package seedu.address.model;

import java.util.function.Predicate;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Student> PREDICATE_SHOW_ALL_PERSONS = unused -> true;
    Predicate<Lesson> PREDICATE_SHOW_ALL_LESSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Student person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Student target);

    /**
     * Archives the given person.
     * The person must exist in the address book.
     */
    void archivePerson(Student target);

    /**
     * Unarchives the given person.
     * The person must exist in the address book.
     */
    void unarchivePerson(Student target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Student person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Student target, Student editedPerson);

    /** Returns an unmodifiable view of the selected active or archived student list. */
    ObservableList<Student> getFilteredPersonList();

    /** Returns an unmodifiable view of the filtered active student list. */
    ObservableList<Student> getFilteredActivePersonList();

    /** Returns an unmodifiable view of the filtered archived student list. */
    ObservableList<Student> getFilteredArchivedPersonList();

    /** Selects which student list filters and indexed commands use, retaining each list's filter. */
    void setArchiveView(boolean archiveView);

    /** Returns whether filters and indexed commands currently use the archived student list. */
    boolean isArchiveView();

    /**
     * Updates the selected student list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Student> predicate);

    /** Updates the selected students and, when viewing active students, their calendar lesson filter. */
    void updateFilteredPersonList(Predicate<Student> predicate, Predicate<Lesson> lessonPredicate);

    /** The calendar observes this even when the same students match two different lesson searches. */
    ObservableValue<Predicate<Lesson>> getLessonPredicate();
}
