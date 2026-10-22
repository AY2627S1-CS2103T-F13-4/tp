package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.person.Student;

/**
 * Lists all persons in the address book to the user.
 */
public class ListArchiveCommand extends Command {

    public static final String COMMAND_WORD = "list-archive";

    public static final String MESSAGE_SUCCESS = "Listed all archived students.";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(Student::isArchived);
        return new CommandResult(MESSAGE_SUCCESS, CommandResult.Screen.STUDENTS);
    }
}
