package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.FollowUpDoneCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for {@link FollowUpDoneCommand}.
 */
public class FollowUpDoneCommandParser implements Parser<FollowUpDoneCommand> {
    private static final int REQUIRED_INDEX_COUNT = 2;

    @Override
    public FollowUpDoneCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpDoneCommand.MESSAGE_USAGE);
        List<Index> indexes;
        try {
            indexes = ParserUtil.parseIndexes(args, REQUIRED_INDEX_COUNT);
        } catch (ParseException e) {
            throw new ParseException(formatError, e);
        }
        return new FollowUpDoneCommand(indexes.getFirst(), indexes.getLast());
    }
}
