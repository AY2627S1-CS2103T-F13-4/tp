package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Student;

/**
 * Shows one student's details in the student table: every lesson with its rate, mode and location, the contact
 * details, guardian and tags. The calendar labels lessons "#3.1", so {@code view 3} opens that lesson's student.
 */
public class ViewCommand extends Command {

    public static final String COMMAND_WORD = "view";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows a student's details, including every lesson's rate, mode and location.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 3";

    public static final String MESSAGE_SUCCESS = "Showing %1$s's details.";

    private final Index targetIndex;

    public ViewCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Student> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Student student = lastShownList.get(targetIndex.getZeroBased());
        return new CommandResult(String.format(MESSAGE_SUCCESS, student.getName()), targetIndex);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof ViewCommand otherViewCommand && targetIndex.equals(otherViewCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("targetIndex", targetIndex).toString();
    }
}
