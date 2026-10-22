package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.model.Model;
import seedu.address.model.person.Student;

/** Shows only the active (not archived) students with outstanding guardian follow-ups. */
public class FollowUpListCommand extends Command {

    public static final String COMMAND_WORD = "followups";

    public static final String MESSAGE_SUCCESS = "%1$d students with outstanding follow-ups listed.";
    public static final String MESSAGE_NONE = "No outstanding follow-ups. Use list to see every student.";

    /** Keeps the active students with at least one outstanding follow-up, as {@code list} hides archived ones. */
    public static final Predicate<Student> PREDICATE_HAS_FOLLOW_UPS = student -> !student.isArchived()
            && student.hasFollowUps();

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(PREDICATE_HAS_FOLLOW_UPS);
        int count = model.getFilteredPersonList().size();
        String feedback = count == 0 ? MESSAGE_NONE : String.format(MESSAGE_SUCCESS, count);
        return new CommandResult(feedback, CommandResult.Screen.STUDENTS);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof FollowUpListCommand;
    }

    @Override
    public int hashCode() {
        return FollowUpListCommand.class.hashCode();
    }
}
