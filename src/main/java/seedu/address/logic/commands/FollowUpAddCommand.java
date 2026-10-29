package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Student;

/**
 * Adds an outstanding follow-up and opens the student details while displayed.
 */
public class FollowUpAddCommand extends Command {
    public static final String COMMAND_WORD = "followup-add";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Records something to follow up on with a student's guardian.\n"
            + "Parameters: STUDENT_INDEX text/TEXT\n"
            + "Example: " + COMMAND_WORD + " 1 text/Send the term 4 progress report";
    public static final String MESSAGE_SUCCESS = "Added follow-up %1$d for %2$s: %3$s";
    public static final String MESSAGE_DUPLICATE_FOLLOW_UP = "This student already has this follow-up.";

    private final Index studentIndex;
    private final FollowUp toAdd;

    /**
     * Creates a command with the specified student index and details.
     */
    public FollowUpAddCommand(Index studentIndex, FollowUp toAdd) {
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
        List<FollowUp> followUps = new ArrayList<>(student.getFollowUps());
        if (followUps.stream().anyMatch(existing -> existing.isSameFollowUp(toAdd))) {
            throw new CommandException(MESSAGE_DUPLICATE_FOLLOW_UP);
        }
        int position = followUps.size();
        followUps.add(toAdd);
        Student updatedStudent = student.withFollowUps(followUps);
        model.setPerson(student, updatedStudent);
        String feedback = String.format(MESSAGE_SUCCESS, Index.fromZeroBased(position).getOneBased(),
                student.getName(), toAdd);
        // A change can remove the student from the current filtered list.
        int shownAt = model.getFilteredPersonList().indexOf(updatedStudent);
        return shownAt < 0
                ? new CommandResult(feedback, CommandResult.Screen.STUDENTS)
                : new CommandResult(feedback, Index.fromZeroBased(shownAt));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof FollowUpAddCommand command
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
