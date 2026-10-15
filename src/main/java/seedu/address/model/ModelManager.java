package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Student> filteredPersons;
    private final SortedList<Student> shownPersons;
    private final ReadOnlyObjectWrapper<Predicate<Lesson>> lessonPredicate =
            new ReadOnlyObjectWrapper<>(PREDICATE_SHOW_ALL_LESSONS);

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        // Shown grouped by school stage (Primary, Secondary, JC, then others), keeping the order added within each.
        // Commands use this list too, so the numbers on screen always match command indices. The tie-break on the
        // stored order matters: without it an edited student can move within its group and change its number.
        ObservableList<Student> stored = this.addressBook.getPersonList();
        shownPersons = new SortedList<>(filteredPersons,
                Comparator.comparing(Student::getStage).thenComparingInt(stored::indexOf));
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Student person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Student target) {
        addressBook.removePerson(target);
    }

    @Override
    public void addPerson(Student person) {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Student target, Student editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
    }

    //=========== Filtered Student List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Student} backed by the internal list of
     * {@code addressBook}, filtered and grouped by school stage
     */
    @Override
    public ObservableList<Student> getFilteredPersonList() {
        return shownPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Student> predicate) {
        updateFilteredPersonList(predicate, PREDICATE_SHOW_ALL_LESSONS);
    }

    @Override
    public void updateFilteredPersonList(Predicate<Student> predicate, Predicate<Lesson> lessonPredicate) {
        requireNonNull(predicate);
        requireNonNull(lessonPredicate);
        filteredPersons.setPredicate(predicate);
        this.lessonPredicate.set(lessonPredicate);
    }

    @Override
    public ObservableValue<Predicate<Lesson>> getLessonPredicate() {
        return lessonPredicate.getReadOnlyProperty();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons)
                && lessonPredicate.get().equals(otherModelManager.lessonPredicate.get());
    }

}
