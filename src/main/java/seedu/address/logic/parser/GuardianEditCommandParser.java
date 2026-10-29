package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.GuardianEditCommand;
import seedu.address.logic.commands.GuardianEditCommand.EditGuardianDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for {@link GuardianEditCommand}.
 */
public class GuardianEditCommandParser implements Parser<GuardianEditCommand> {

    @Override
    public GuardianEditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, GuardianEditCommand.MESSAGE_USAGE);
        ArgumentMultimap arguments = GuardianCommandParserUtil.tokenize(args);
        Index studentIndex;
        try {
            studentIndex = ParserUtil.parseIndex(arguments.getPreamble());
        } catch (ParseException e) {
            throw new ParseException(formatError, e);
        }
        EditGuardianDescriptor descriptor = new EditGuardianDescriptor();
        if (arguments.getValue(PREFIX_NAME).isPresent()) {
            descriptor.setName(ParserUtil.parseName(arguments.getValue(PREFIX_NAME).orElseThrow()));
        }
        if (arguments.getValue(PREFIX_PHONE).isPresent()) {
            descriptor.setPhone(ParserUtil.parsePhone(arguments.getValue(PREFIX_PHONE).orElseThrow()));
        }
        if (!descriptor.isAnyFieldEdited()) {
            throw new ParseException(formatError);
        }
        return new GuardianEditCommand(studentIndex, descriptor);
    }
}
