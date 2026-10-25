package seedu.address.logic;

import java.util.function.Predicate;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;

/**
 * API of the Logic component
 */
public interface Logic {
    /**
     * Executes the command and returns the result.
     * @param commandText The command as entered by the user.
     * @return the result of the command execution.
     * @throws CommandException If an error occurs during command execution.
     * @throws ParseException If an error occurs during parsing.
     */
    CommandResult execute(String commandText) throws CommandException, ParseException;

    /**
     * Returns the AddressBook.
     *
     * @see seedu.address.model.Model#getAddressBook()
     */
    ReadOnlyAddressBook getAddressBook();

    /** Returns an unmodifiable view of the selected active or archived student list. */
    ObservableList<Student> getFilteredPersonList();

    /** Returns an unmodifiable view of the filtered active student list. */
    ObservableList<Student> getFilteredActivePersonList();

    /** Returns an unmodifiable view of the filtered archived student list. */
    ObservableList<Student> getFilteredArchivedPersonList();

    /** Returns whether commands currently use the archived student list. */
    boolean isArchiveView();

    /** Returns the active calendar lesson filter. */
    ObservableValue<Predicate<Lesson>> getLessonPredicate();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Set the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);
}
