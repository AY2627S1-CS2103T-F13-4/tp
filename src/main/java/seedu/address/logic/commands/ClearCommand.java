package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.person.Student;

/**
 * Clears the address book, or only the archived students when the Archives are shown.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "All students have been deleted.";
    public static final String MESSAGE_ARCHIVE_SUCCESS =
            "All archived students have been deleted. Active students are kept.";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        if (model.isArchiveView()) {
            // Copy first, as deleting changes the list being read
            List<Student> archived = List.copyOf(model.getAddressBook().getArchivedPersonList());
            archived.forEach(model::deletePerson);
            return new CommandResult(MESSAGE_ARCHIVE_SUCCESS);
        }
        model.setAddressBook(new AddressBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
