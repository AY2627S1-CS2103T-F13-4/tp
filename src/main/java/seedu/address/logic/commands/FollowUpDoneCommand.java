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
 * Completes an outstanding follow-up and opens the student details while displayed.
 */
public class FollowUpDoneCommand extends Command {
    public static final String COMMAND_WORD = "followup-done";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Marks one of a student's follow-ups as done, which removes it\n"
            + "Parameters: STUDENT_INDEX FOLLOW_UP_INDEX\n"
            + "Example: " + COMMAND_WORD + " 1 2";
    public static final String MESSAGE_DONE_FOLLOW_UP_SUCCESS = "Completed follow-up %1$d for %2$s: %3$s";

    private final Index studentIndex;
    private final Index followUpIndex;

    /**
     * Creates a command with the specified student index and details.
     */
    public FollowUpDoneCommand(Index studentIndex, Index followUpIndex) {
        requireAllNonNull(studentIndex, followUpIndex);
        this.studentIndex = studentIndex;
        this.followUpIndex = followUpIndex;
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
        if (followUpIndex.getZeroBased() >= followUps.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_FOLLOW_UP_DISPLAYED_INDEX);
        }
        FollowUp removed = followUps.remove(followUpIndex.getZeroBased());
        Student updatedStudent = student.withFollowUps(followUps);
        model.setPerson(student, updatedStudent);
        String feedback = String.format(MESSAGE_DONE_FOLLOW_UP_SUCCESS, followUpIndex.getOneBased(),
                student.getName(), removed);
        // A change can remove the student from the current filtered list.
        int shownAt = model.getFilteredPersonList().indexOf(updatedStudent);
        return shownAt < 0
                ? new CommandResult(feedback, CommandResult.Screen.STUDENTS)
                : new CommandResult(feedback, Index.fromZeroBased(shownAt));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof FollowUpDoneCommand command
                && studentIndex.equals(command.studentIndex) && followUpIndex.equals(command.followUpIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex, followUpIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("followUpIndex", followUpIndex)
                .toString();
    }
}
