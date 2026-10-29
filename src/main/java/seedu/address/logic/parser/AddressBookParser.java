package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.FollowUpAddCommand;
import seedu.address.logic.commands.FollowUpDoneCommand;
import seedu.address.logic.commands.FollowUpEditCommand;
import seedu.address.logic.commands.FollowUpListCommand;
import seedu.address.logic.commands.GuardianAddCommand;
import seedu.address.logic.commands.GuardianDeleteCommand;
import seedu.address.logic.commands.GuardianEditCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.commands.LessonDeleteCommand;
import seedu.address.logic.commands.LessonEditCommand;
import seedu.address.logic.commands.ListArchiveCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NoteAddCommand;
import seedu.address.logic.commands.NoteDeleteCommand;
import seedu.address.logic.commands.NoteEditCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.UnarchiveCommand;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.commands.WeekCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses user input.
 */
public class AddressBookParser {

    /**
     * Used for initial separation of command word and args.
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");

    /**
     * Parses user input into command for execution.
     *
     * @param userInput full user input string
     * @return the command based on the user input
     * @throws ParseException if the user input does not conform to the expected format
     */
    public Command parseCommand(String userInput) throws ParseException {
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(userInput.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }

        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");

        return switch (commandWord) {
            case GuardianAddCommand.COMMAND_WORD -> new GuardianAddCommandParser().parse(arguments);
            case GuardianEditCommand.COMMAND_WORD -> new GuardianEditCommandParser().parse(arguments);
            case GuardianDeleteCommand.COMMAND_WORD -> new GuardianDeleteCommandParser().parse(arguments);
            case LessonAddCommand.COMMAND_WORD -> new LessonAddCommandParser().parse(arguments);
            case LessonEditCommand.COMMAND_WORD -> new LessonEditCommandParser().parse(arguments);
            case LessonDeleteCommand.COMMAND_WORD -> new LessonDeleteCommandParser().parse(arguments);
            case NoteAddCommand.COMMAND_WORD -> new NoteAddCommandParser().parse(arguments);
            case NoteDeleteCommand.COMMAND_WORD -> new NoteDeleteCommandParser().parse(arguments);
            case NoteEditCommand.COMMAND_WORD -> new NoteEditCommandParser().parse(arguments);
            case FollowUpAddCommand.COMMAND_WORD -> new FollowUpAddCommandParser().parse(arguments);
            case FollowUpEditCommand.COMMAND_WORD -> new FollowUpEditCommandParser().parse(arguments);
            case FollowUpDoneCommand.COMMAND_WORD -> new FollowUpDoneCommandParser().parse(arguments);
            case FollowUpListCommand.COMMAND_WORD -> new FollowUpListCommand();
            case AddCommand.COMMAND_WORD -> new AddCommandParser().parse(arguments);
            case EditCommand.COMMAND_WORD -> new EditCommandParser().parse(arguments);
            case DeleteCommand.COMMAND_WORD -> new DeleteCommandParser().parse(arguments);
            case ClearCommand.COMMAND_WORD -> new ClearCommand();
            case FindCommand.COMMAND_WORD -> new FindCommandParser().parse(arguments);
            case ListCommand.COMMAND_WORD -> new ListCommand();
            case ListArchiveCommand.COMMAND_WORD -> new ListArchiveCommand();
            case WeekCommand.COMMAND_WORD -> new WeekCommand();
            case SortCommand.COMMAND_WORD -> new SortCommandParser().parse(arguments);
            case ViewCommand.COMMAND_WORD -> new ViewCommandParser().parse(arguments);
            case ArchiveCommand.COMMAND_WORD -> new ArchiveCommandParser().parse(arguments);
            case UnarchiveCommand.COMMAND_WORD -> new UnarchiveCommandParser().parse(arguments);
            case ExitCommand.COMMAND_WORD -> new ExitCommand();
            case HelpCommand.COMMAND_WORD -> new HelpCommand();
            default -> {
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }

}
