package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.model.Model;
import seedu.address.model.person.RateSortOrder;

/**
 * Lists all persons in the address book to the user, in the order they were added within each stage.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_SUCCESS = "Listed all students.";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setArchiveView(false);
        model.setRateSortOrder(RateSortOrder.NONE);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(MESSAGE_SUCCESS, CommandResult.Screen.STUDENTS);
    }
}
