package seedu.address.ui;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ListArchiveCommand;
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
        return emptyTitle(total, false);
    }

    /** Returns an empty-state heading for the selected student list. */
    static String emptyTitle(int total, boolean archived) {
        if (archived && total == 0) {
            return "No archived students yet";
        }
        return total == 0 ? "No students yet" : "No students match";
    }

    /** Returns the next step shown when the list has no rows. Its command is {@link #emptyCommand}. */
    static String emptyDetail(int total) {
        return emptyDetail(total, false);
    }

    /** Returns the next step for an empty active or archived student list. */
    static String emptyDetail(int total, boolean archived) {
        if (archived && total == 0) {
            return "View your active students with:";
        }
        if (total == 0) {
            return "Add your first student, for example:";
        }
        return "Show all " + total + (total == 1 ? " student" : " students") + " with:";
    }

    /** Returns a command that can be typed as shown to take the next step, e.g. "list". */
    static String emptyCommand(int total) {
        return emptyCommand(total, false);
    }

    /** Returns the command for the selected student list's empty state. */
    static String emptyCommand(int total, boolean archived) {
        if (archived) {
            return total == 0 ? ListCommand.COMMAND_WORD : ListArchiveCommand.COMMAND_WORD;
        }
        if (total == 0) {
            return AddCommand.COMMAND_WORD + " " + PREFIX_NAME + "Alex Tan " + PREFIX_PHONE + "91234567 "
                    + PREFIX_EMAIL + "alex@example.com " + PREFIX_ADDRESS + "Blk 12 Clementi Ave 3";
        }
        return ListCommand.COMMAND_WORD;
    }
}
