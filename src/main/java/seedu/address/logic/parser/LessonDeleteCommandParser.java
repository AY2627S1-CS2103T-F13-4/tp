package seedu.address.logic.parser;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LessonDeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

import java.util.List;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

/**
 * Parses input arguments and creates a new LessonDeleteCommand object.
 */
public class LessonDeleteCommandParser implements Parser<LessonDeleteCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the LessonDeleteCommand
     * and returns a LessonDeleteCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public LessonDeleteCommand parse(String args) throws ParseException {
        try {
            List<Index> indexes = ParserUtil.parseIndexes(args, 2);
            Index studentIndex = indexes.get(0);
            Index lessonIndex = indexes.get(1);
            return new LessonDeleteCommand(studentIndex, lessonIndex);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, LessonDeleteCommand.MESSAGE_USAGE), pe);
        }
    }
}
