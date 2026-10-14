package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.model.Model;

/**
 * Shows every student's recurring lessons on the weekly calendar.
 */
public class WeekCommand extends Command {

    public static final String COMMAND_WORD = "week";

    public static final String MESSAGE_SUCCESS = "Showing this week's lessons.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        // The calendar labels each lesson with its student's number, so every student must be listed
        // for those numbers to match the indices that commands use.
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(MESSAGE_SUCCESS, CommandResult.Screen.WEEK);
    }
}
