package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LessonDeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new LessonDeleteCommand object.
 */
public class LessonDeleteCommandParser implements Parser<LessonDeleteCommand> {
    private static final int REQUIRED_INDEX_COUNT = 2;

    /**
     * Parses the given {@code String} of arguments in the context of the LessonDeleteCommand
     * and returns a LessonDeleteCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    @Override
    public LessonDeleteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        try {
            List<Index> indexes = ParserUtil.parseIndexes(args, REQUIRED_INDEX_COUNT);
            Index studentIndex = indexes.getFirst();
            Index lessonIndex = indexes.getLast();
            return new LessonDeleteCommand(studentIndex, lessonIndex);
        } catch (ParseException pe) {
            throw new ParseException(String.format(
                    MESSAGE_INVALID_COMMAND_FORMAT, LessonDeleteCommand.MESSAGE_USAGE), pe);
        }
    }
}
