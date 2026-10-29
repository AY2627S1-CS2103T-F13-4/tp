package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Student;

/**
 * Unarchives a person in the address book.
 */
public class UnarchiveCommand extends Command {

    public static final String COMMAND_WORD = "unarchive";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Unarchives the student identified by the index number used in the displayed student list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_UNARCHIVE_PERSON_SUCCESS = "Unarchived student: %1$s";
    public static final String MESSAGE_ALREADY_UNARCHIVED = "This student is already unarchived.";

    private final Index index;

    /**
     * @param index of the person in the filtered person list to archive
     */
    public UnarchiveCommand(Index index) {
        this.index = index;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Student> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Student personToUnarchive = lastShownList.get(index.getZeroBased());
        if (!personToUnarchive.isArchived()) {
            throw new CommandException(MESSAGE_ALREADY_UNARCHIVED);
        }

        model.unarchivePerson(personToUnarchive);

        String message = String.format(MESSAGE_UNARCHIVE_PERSON_SUCCESS, Messages.format(personToUnarchive));
        // Archived lessons were not checked against active ones, so returning the student can double-book.
        Student unarchived = personToUnarchive.withArchived(false);
        List<Student> activeStudents = model.getAddressBook().getPersonList();
        StringBuilder warnings = new StringBuilder();
        for (int i = 0; i < unarchived.getLessons().size(); i++) {
            warnings.append(LessonClashes.warning(activeStudents, unarchived, i));
        }
        return warnings.isEmpty() ? new CommandResult(message) : CommandResult.warning(message + warnings);
    }
}
