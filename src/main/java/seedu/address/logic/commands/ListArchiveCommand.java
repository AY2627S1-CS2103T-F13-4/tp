package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.model.Model;

/**
 * Lists all archived students in the address book to the user.
 */
public class ListArchiveCommand extends Command {

    public static final String COMMAND_WORD = "list-archive";

    public static final String MESSAGE_SUCCESS = "Listed all archived students.";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setArchiveView(true);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(MESSAGE_SUCCESS, CommandResult.Screen.ARCHIVE);
    }
}
