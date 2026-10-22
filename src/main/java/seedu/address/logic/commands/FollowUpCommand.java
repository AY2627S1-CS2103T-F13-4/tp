package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Student;

/**
 * Changes only the outstanding guardian follow-ups of a student in the displayed list, then shows that student's
 * details while the student is still displayed.
 */
public class FollowUpCommand extends Command {
    public static final String ADD_COMMAND_WORD = "followup-add";
    public static final String EDIT_COMMAND_WORD = "followup-edit";
    public static final String DONE_COMMAND_WORD = "followup-done";
    public static final String MESSAGE_ADD_USAGE = ADD_COMMAND_WORD
            + ": Records something to follow up on with a student's guardian.\n"
            + "Parameters: STUDENT_INDEX text/TEXT\n"
            + "Example: " + ADD_COMMAND_WORD + " 1 text/Send the term 4 progress report";
    public static final String MESSAGE_EDIT_USAGE = EDIT_COMMAND_WORD
            + ": Changes the text of one of a student's follow-ups, numbered as in the student's details.\n"
            + "Parameters: STUDENT_INDEX FOLLOW_UP_INDEX text/TEXT\n"
            + "Example: " + EDIT_COMMAND_WORD + " 1 2 text/Call about the make-up lesson on Saturday";
    public static final String MESSAGE_DONE_USAGE = DONE_COMMAND_WORD
            + ": Marks one of a student's follow-ups as done, which removes it.\n"
            + "Parameters: STUDENT_INDEX FOLLOW_UP_INDEX\n"
            + "Example: " + DONE_COMMAND_WORD + " 1 2";
    public static final String MESSAGE_DUPLICATE_FOLLOW_UP = "This student already has this follow-up.";
    public static final String MESSAGE_INVALID_FOLLOW_UP_DISPLAYED_INDEX = "The follow-up index provided is invalid.";
    public static final String MESSAGE_ADDED = "Added follow-up %1$d for %2$s: %3$s";
    public static final String MESSAGE_EDITED = "Edited follow-up %1$d for %2$s: %3$s";
    public static final String MESSAGE_DONE = "Completed follow-up %1$d for %2$s: %3$s";

    /** Supported follow-up mutations. Listing uses {@link FollowUpListCommand}. */
    public enum Operation { ADD, EDIT, DONE }

    private final Index studentIndex;
    private final Operation operation;
    private final Optional<Index> followUpIndex;
    private final Optional<FollowUp> followUp;

    /**
     * Creates a validated follow-up mutation. Adding takes only {@code followUp}, editing takes both, and marking
     * one as done takes only {@code followUpIndex}.
     */
    public FollowUpCommand(Index studentIndex, Operation operation, Optional<Index> followUpIndex,
            Optional<FollowUp> followUp) {
        this.studentIndex = requireNonNull(studentIndex);
        this.operation = requireNonNull(operation);
        this.followUpIndex = requireNonNull(followUpIndex);
        this.followUp = requireNonNull(followUp);
        boolean isValid = switch (operation) {
            case ADD -> followUpIndex.isEmpty() && followUp.isPresent();
            case EDIT -> followUpIndex.isPresent() && followUp.isPresent();
            case DONE -> followUpIndex.isPresent() && followUp.isEmpty();
        };
        if (!isValid) {
            throw new IllegalArgumentException("Invalid fields for follow-up operation.");
        }
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (studentIndex.getZeroBased() >= model.getFilteredPersonList().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Student student = model.getFilteredPersonList().get(studentIndex.getZeroBased());
        List<FollowUp> followUps = new ArrayList<>(student.getFollowUps());
        int position = followUpIndex.map(Index::getZeroBased).orElse(followUps.size());
        if (operation != Operation.ADD && position >= followUps.size()) {
            throw new CommandException(MESSAGE_INVALID_FOLLOW_UP_DISPLAYED_INDEX);
        }

        String message = switch (operation) {
            case ADD -> {
                requireNoDuplicate(followUps, followUp.orElseThrow(), -1);
                followUps.add(followUp.orElseThrow());
                yield MESSAGE_ADDED;
            }
            case EDIT -> {
                requireNoDuplicate(followUps, followUp.orElseThrow(), position);
                followUps.set(position, followUp.orElseThrow());
                yield MESSAGE_EDITED;
            }
            case DONE -> MESSAGE_DONE;
        };
        FollowUp shown = operation == Operation.DONE ? followUps.remove(position) : followUp.orElseThrow();

        Student replacement = new Student(student.getName(), student.getPhone(), student.getEmail(),
                student.getAddress(), student.getTags(), student.getSchool(), student.getSchoolingLevel(),
                student.getGuardian(), student.getLessons(), student.isArchived(), student.getNotes(),
                followUps);
        model.setPerson(student, replacement);
        String feedback = String.format(message, position + 1, student.getName(), shown);

        // After `followups`, a student whose last item is done leaves the list, so another student now holds
        // their number. Open the student only while they are still displayed.
        int shownAt = model.getFilteredPersonList().indexOf(replacement);
        return shownAt < 0
                ? new CommandResult(feedback, CommandResult.Screen.STUDENTS)
                : new CommandResult(feedback, Index.fromZeroBased(shownAt));
    }

    /** Throws if {@code candidate} reads the same as an item other than the one at {@code ignoredPosition}. */
    private static void requireNoDuplicate(List<FollowUp> followUps, FollowUp candidate, int ignoredPosition)
            throws CommandException {
        for (int i = 0; i < followUps.size(); i++) {
            if (i != ignoredPosition && followUps.get(i).isSameFollowUp(candidate)) {
                throw new CommandException(MESSAGE_DUPLICATE_FOLLOW_UP);
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof FollowUpCommand command
                && studentIndex.equals(command.studentIndex) && operation == command.operation
                && followUpIndex.equals(command.followUpIndex) && followUp.equals(command.followUp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentIndex, operation, followUpIndex, followUp);
    }
}
