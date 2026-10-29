package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.model.Model;
import seedu.address.model.person.RateSortOrder;

/**
 * Lists all persons in the address book to the user, by level then name within each stage.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_SUCCESS = "Listed all active students";
    public static final String MESSAGE_NONE = "No active students to list";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setArchiveView(false);
        model.setRateSortOrder(RateSortOrder.NONE);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        String feedback = model.getFilteredPersonList().isEmpty() ? MESSAGE_NONE : MESSAGE_SUCCESS;
        return new CommandResult(feedback, CommandResult.Screen.STUDENTS);
    }
}
