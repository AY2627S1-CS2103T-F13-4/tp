package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Student;

/**
 * Adds a guardian to a student in the displayed list.
 */
public class GuardianAddCommand extends Command {
    public static final String COMMAND_WORD = "guardian-add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + " INDEX name/NAME phone/PHONE\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_NAME + "Jonathan Doe "
            + PREFIX_PHONE + "90123456 ";

    public static final String MESSAGE_SUCCESS = "Guardian added for %1$s.";
    public static final String MESSAGE_ALREADY_EXISTS = "This student already has a guardian. Use guardian-edit.";


    private final Index studentIndex;
    private final Guardian toAdd;

    /**
     * Creates a command with the specified student index and details.
     */
    public GuardianAddCommand(Index studentIndex, Guardian toAdd) {
        requireAllNonNull(studentIndex, toAdd);
        this.studentIndex = studentIndex;
        this.toAdd = toAdd;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Student> lastShownList = model.getFilteredPersonList();
        if (studentIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Student student = lastShownList.get(studentIndex.getZeroBased());
        if (student.getGuardian().isPresent()) {
            throw new CommandException(MESSAGE_ALREADY_EXISTS);
        }
        model.setPerson(student, student.withGuardian(Optional.of(toAdd)));
        return new CommandResult(String.format(MESSAGE_SUCCESS, student.getName()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof GuardianAddCommand command
                && studentIndex.equals(command.studentIndex) && toAdd.equals(command.toAdd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex, toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("toAdd", toAdd)
                .toString();
    }
}
