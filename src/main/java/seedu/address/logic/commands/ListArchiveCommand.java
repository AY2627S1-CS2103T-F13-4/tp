package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.model.Model;

/**
 * Lists all archived students in the address book to the user.
 */
public class ListArchiveCommand extends Command {

    public static final String COMMAND_WORD = "list-archive";

    public static final String MESSAGE_SUCCESS = "Listed all archived students";
    public static final String MESSAGE_NONE = "No archived students to list";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setArchiveView(true);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        String feedback = model.getFilteredPersonList().isEmpty() ? MESSAGE_NONE : MESSAGE_SUCCESS;
        return new CommandResult(feedback, CommandResult.Screen.ARCHIVE);
    }
}
