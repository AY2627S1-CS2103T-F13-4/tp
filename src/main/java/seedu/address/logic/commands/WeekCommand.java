package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

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
        // Keep the current find results; calendar numbers use the same displayed student list.
        return new CommandResult(MESSAGE_SUCCESS, CommandResult.Screen.WEEK);
    }
}
