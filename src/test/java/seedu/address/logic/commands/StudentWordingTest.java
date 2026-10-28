package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;

/** Guards against the inherited AddressBook wording returning to messages that users read. */
public class StudentWordingTest {

    private static final List<String> USER_FACING_MESSAGES = List.of(
            Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX,
            AddCommand.MESSAGE_USAGE, AddCommand.MESSAGE_SUCCESS, AddCommand.MESSAGE_DUPLICATE_PERSON,
            EditCommand.MESSAGE_USAGE, EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, EditCommand.MESSAGE_DUPLICATE_PERSON,
            DeleteCommand.MESSAGE_USAGE, DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
            ArchiveCommand.MESSAGE_USAGE, ArchiveCommand.MESSAGE_ARCHIVE_PERSON_SUCCESS,
            ArchiveCommand.MESSAGE_ALREADY_ARCHIVED,
            UnarchiveCommand.MESSAGE_USAGE, UnarchiveCommand.MESSAGE_UNARCHIVE_PERSON_SUCCESS,
            UnarchiveCommand.MESSAGE_ALREADY_UNARCHIVED,
            ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT);

    @Test
    public void userFacingMessages_sayStudentNotPerson() {
        for (String message : USER_FACING_MESSAGES) {
            String lower = message.toLowerCase(Locale.ROOT);
            assertFalse(lower.contains("person"), message);
            assertFalse(lower.contains("address book"), message);
        }
    }
}
