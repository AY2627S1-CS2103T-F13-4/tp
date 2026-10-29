package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.commands.FollowUpAddCommand.MESSAGE_DUPLICATE_FOLLOW_UP;

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
 * Edits an outstanding follow-up and opens the student details while displayed.
 */
public class FollowUpEditCommand extends Command {
    public static final String COMMAND_WORD = "followup-edit";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Changes the text of one of a student's follow-ups, numbered as in the student's details.\n"
            + "Parameters: STUDENT_INDEX FOLLOW_UP_INDEX text/TEXT\n"
            + "Example: " + COMMAND_WORD + " 1 2 text/Call about the make-up lesson on Saturday";
    public static final String MESSAGE_EDIT_FOLLOW_UP_SUCCESS = "Edited follow-up %1$d for %2$s: %3$s";

    private final Index studentIndex;
    private final Index followUpIndex;
    private final FollowUp toEdit;

    /**
     * Creates a command with the specified student index and details.
     */
    public FollowUpEditCommand(Index studentIndex, Index followUpIndex, FollowUp toEdit) {
        requireAllNonNull(studentIndex, followUpIndex, toEdit);
        this.studentIndex = studentIndex;
        this.followUpIndex = followUpIndex;
        this.toEdit = toEdit;
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
        int position = followUpIndex.getZeroBased();
        if (position >= followUps.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_FOLLOW_UP_DISPLAYED_INDEX);
        }
        for (int i = 0; i < followUps.size(); i++) {
            if (i != position && followUps.get(i).isSameFollowUp(toEdit)) {
                throw new CommandException(MESSAGE_DUPLICATE_FOLLOW_UP);
            }
        }
        followUps.set(position, toEdit);
        Student updatedStudent = student.withFollowUps(followUps);
        model.setPerson(student, updatedStudent);
        String feedback = String.format(MESSAGE_EDIT_FOLLOW_UP_SUCCESS, followUpIndex.getOneBased(),
                student.getName(), toEdit);
        // A change can remove the student from the current filtered list.
        int shownAt = model.getFilteredPersonList().indexOf(updatedStudent);
        return shownAt < 0
                ? new CommandResult(feedback, CommandResult.Screen.STUDENTS)
                : new CommandResult(feedback, Index.fromZeroBased(shownAt));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof FollowUpEditCommand command
                && studentIndex.equals(command.studentIndex)
                && followUpIndex.equals(command.followUpIndex)
                && toEdit.equals(command.toEdit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex, followUpIndex, toEdit);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("studentIndex", studentIndex)
                .add("followUpIndex", followUpIndex)
                .add("toEdit", toEdit)
                .toString();
    }
}
