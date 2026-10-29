package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.GuardianAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

/**
 * Parses arguments for {@link GuardianAddCommand}.
 */
public class GuardianAddCommandParser implements Parser<GuardianAddCommand> {

    @Override
    public GuardianAddCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, GuardianAddCommand.MESSAGE_USAGE);
        ArgumentMultimap arguments = GuardianCommandParserUtil.tokenize(args);
        Index studentIndex;
        try {
            studentIndex = ParserUtil.parseIndex(arguments.getPreamble());
        } catch (ParseException e) {
            throw new ParseException(formatError, e);
        }
        if (arguments.getValue(PREFIX_NAME).isEmpty() || arguments.getValue(PREFIX_PHONE).isEmpty()) {
            throw new ParseException(formatError);
        }
        Name name = ParserUtil.parseName(arguments.getValue(PREFIX_NAME).orElseThrow());
        Phone phone = ParserUtil.parsePhone(arguments.getValue(PREFIX_PHONE).orElseThrow());
        return new GuardianAddCommand(studentIndex, new Guardian(name, phone));
    }
}
