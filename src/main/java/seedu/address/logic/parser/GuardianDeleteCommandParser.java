package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.GuardianDeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for {@link GuardianDeleteCommand}.
 */
public class GuardianDeleteCommandParser implements Parser<GuardianDeleteCommand> {

    @Override
    public GuardianDeleteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        try {
            return new GuardianDeleteCommand(ParserUtil.parseIndex(args));
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    GuardianDeleteCommand.MESSAGE_USAGE), e);
        }
    }
}
