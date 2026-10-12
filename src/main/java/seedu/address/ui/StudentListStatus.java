package seedu.address.ui;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ListCommand;

/**
 * Text for the student list header and its empty state.
 * It tells an empty address book apart from a search that matched no one, since each needs a different next step.
 */
final class StudentListStatus {

    private StudentListStatus() {}

    /** Returns the count shown beside the list title, e.g. "8 students" or "Showing 1 of 8". */
    static String summary(int shown, int total) {
        if (total == 0) {
            return "";
        }
        if (shown == total) {
            return total + (total == 1 ? " student" : " students");
        }
        return "Showing " + shown + " of " + total;
    }

    /** Returns the heading shown when the list has no rows. */
    static String emptyTitle(int total) {
        return total == 0 ? "No students yet" : "No students match";
    }

    /** Returns the next step shown when the list has no rows. */
    static String emptyDetail(int total) {
        if (total == 0) {
            return "Add one with: " + AddCommand.COMMAND_WORD + " " + PREFIX_NAME + "NAME " + PREFIX_PHONE + "PHONE "
                    + PREFIX_EMAIL + "EMAIL " + PREFIX_ADDRESS + "ADDRESS";
        }
        return "Type " + ListCommand.COMMAND_WORD + " to show all " + total + (total == 1 ? " student." : " students.");
    }
}
