package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ArchiveCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.FollowUpListCommand;
import seedu.address.logic.commands.GuardianCommand;
import seedu.address.logic.commands.GuardianCommand.Operation;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.commands.LessonCommand;
import seedu.address.logic.commands.LessonDeleteCommand;
import seedu.address.logic.commands.LessonEditCommand;
import seedu.address.logic.commands.ListArchiveCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NoteCommand;
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
    private static final Logger logger = LogsCenter.getLogger(AddressBookParser.class);

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

        // Note to developers: Change LOG_LEVEL in LogsCenter to enable lower level (i.e., FINE, FINER and lower)
        // log messages such as the one below.
        // Lower level log messages are used sparingly to minimize noise in the code.
        logger.fine("Command word: " + commandWord + "; Arguments: " + arguments);

        return switch (commandWord) {
            case GuardianCommand.ADD_COMMAND_WORD -> new GuardianCommandParser(Operation.ADD).parse(arguments);
            case GuardianCommand.EDIT_COMMAND_WORD -> new GuardianCommandParser(Operation.EDIT).parse(arguments);
            case GuardianCommand.DELETE_COMMAND_WORD -> new GuardianCommandParser(Operation.DELETE).parse(arguments);
            case LessonAddCommand.COMMAND_WORD -> new LessonAddCommandParser().parse(arguments);
            case LessonEditCommand.COMMAND_WORD -> new LessonEditCommandParser().parse(arguments);
            case LessonDeleteCommand.COMMAND_WORD -> new LessonDeleteCommandParser().parse(arguments);
            case NoteCommand.ADD_COMMAND_WORD -> new NoteCommandParser(NoteCommand.Operation.ADD).parse(arguments);
            case NoteCommand.DELETE_COMMAND_WORD ->
                new NoteCommandParser(NoteCommand.Operation.DELETE).parse(arguments);
            case FollowUpCommand.ADD_COMMAND_WORD ->
                new FollowUpCommandParser(FollowUpCommand.Operation.ADD).parse(arguments);
            case FollowUpCommand.EDIT_COMMAND_WORD ->
                new FollowUpCommandParser(FollowUpCommand.Operation.EDIT).parse(arguments);
            case FollowUpCommand.DONE_COMMAND_WORD ->
                new FollowUpCommandParser(FollowUpCommand.Operation.DONE).parse(arguments);
            case FollowUpListCommand.COMMAND_WORD -> new FollowUpListCommand();
            case AddCommand.COMMAND_WORD -> new AddCommandParser().parse(arguments);
            case EditCommand.COMMAND_WORD -> new EditCommandParser().parse(arguments);
            case DeleteCommand.COMMAND_WORD -> new DeleteCommandParser().parse(arguments);
            case ClearCommand.COMMAND_WORD -> new ClearCommand();
            case FindCommand.COMMAND_WORD -> new FindCommandParser().parse(arguments);
            case ListCommand.COMMAND_WORD -> new ListCommand();
            case ListArchiveCommand.COMMAND_WORD -> new ListArchiveCommand();
            case WeekCommand.COMMAND_WORD -> new WeekCommand();
            case ViewCommand.COMMAND_WORD -> new ViewCommandParser().parse(arguments);
            case ArchiveCommand.COMMAND_WORD -> new ArchiveCommandParser().parse(arguments);
            case UnarchiveCommand.COMMAND_WORD -> new UnarchiveCommandParser().parse(arguments);
            case ExitCommand.COMMAND_WORD -> new ExitCommand();
            case HelpCommand.COMMAND_WORD -> new HelpCommand();
            default -> {
                logger.finer("This user input caused a ParseException: " + userInput);
                throw new ParseException(MESSAGE_UNKNOWN_COMMAND);
            }
        };
    }

}
