package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Student;

/**
 * Archives a person in the address book.
 */
public class ArchiveCommand extends Command {

    public static final String COMMAND_WORD = "archive";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Archives the student identified by the index number used in the displayed student list\n"
            + "Parameters: STUDENT_INDEX\n"
            + "Example: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_ARCHIVE_PERSON_SUCCESS = "Archived student: %1$s";
    public static final String MESSAGE_ALREADY_ARCHIVED = "This student is already archived";

    private final Index index;

    /**
     * @param index of the person in the filtered person list to archive
     */
    public ArchiveCommand(Index index) {
        this.index = index;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Student> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Student personToArchive = lastShownList.get(index.getZeroBased());
        if (personToArchive.isArchived()) {
            throw new CommandException(MESSAGE_ALREADY_ARCHIVED);
        }

        model.archivePerson(personToArchive);

        return new CommandResult(String.format(MESSAGE_ARCHIVE_PERSON_SUCCESS, Messages.format(personToArchive)));
    }
}
