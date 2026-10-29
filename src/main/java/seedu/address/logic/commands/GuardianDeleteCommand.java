package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.commands.GuardianEditCommand.MESSAGE_NOT_FOUND;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Student;

/**
 * Removes the guardian of a student in the displayed list.
 */
public class GuardianDeleteCommand extends Command {
    public static final String COMMAND_WORD = "guardian-delete";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " INDEX\n"
            + "Example: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_DELETE_GUARDIAN_SUCCESS = "Guardian removed for %1$s.";

    private final Index studentIndex;

    /**
     * Creates a command with the specified student index and details.
     */
    public GuardianDeleteCommand(Index studentIndex) {
        requireAllNonNull(studentIndex);
        this.studentIndex = studentIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Student> lastShownList = model.getFilteredPersonList();
        if (studentIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Student student = lastShownList.get(studentIndex.getZeroBased());
        if (student.getGuardian().isEmpty()) {
            throw new CommandException(MESSAGE_NOT_FOUND);
        }
        model.setPerson(student, student.withGuardian(Optional.empty()));
        return new CommandResult(String.format(MESSAGE_DELETE_GUARDIAN_SUCCESS, student.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof GuardianDeleteCommand command
                && studentIndex.equals(command.studentIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .toString();
    }
}
