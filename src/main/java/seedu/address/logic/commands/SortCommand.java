package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;
import seedu.address.model.person.RateSortOrder;

/** Orders the shown students by hourly rate within each school stage. */
public class SortCommand extends Command {

    public static final String COMMAND_WORD = "sort";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Sorts students by hourly rate within each schooling level.\n"
            + "Parameters: rate asc | rate desc\n"
            + "Example: " + COMMAND_WORD + " rate desc";

    public static final String MESSAGE_ASCENDING = "Sorted by rate (low to high) within each level. "
            + "A student with several lessons is ranked by their lowest rate; students without lessons come last.";
    public static final String MESSAGE_DESCENDING = "Sorted by rate (high to low) within each level. "
            + "A student with several lessons is ranked by their highest rate; students without lessons come last.";

    private final RateSortOrder order;

    /** Creates a command that sorts by rate in the given direction. */
    public SortCommand(RateSortOrder order) {
        requireNonNull(order);
        assert order != RateSortOrder.NONE : "use list to remove the sort";
        this.order = order;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setRateSortOrder(order);
        String message = order == RateSortOrder.ASCENDING ? MESSAGE_ASCENDING : MESSAGE_DESCENDING;
        return new CommandResult(message, CommandResult.Screen.STUDENTS);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof SortCommand sortCommand && order == sortCommand.order;
    }

    @Override
    public int hashCode() {
        return order.hashCode();
    }
}
