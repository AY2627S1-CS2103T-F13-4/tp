package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TEXT;

import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.FollowUpAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUp;

/**
 * Parses follow-up text. Follow-up text is free text: everything after {@code text/} is the text,
 * including words such as {@code and/or} or {@code text/}.
 */
public class FollowUpAddCommandParser implements Parser<FollowUpAddCommand> {

    @Override
    public FollowUpAddCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpAddCommand.MESSAGE_USAGE);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenizeWithTrailingValue(args, PREFIX_TEXT);
        Index studentIndex;
        try {
            studentIndex = ParserUtil.parseIndex(arguments.getPreamble());
        } catch (ParseException e) {
            throw new ParseException(formatError, e);
        }
        Optional<String> text = arguments.getValue(PREFIX_TEXT);
        if (text.isEmpty()) {
            throw new ParseException(formatError);
        }
        FollowUp followUp = ParserUtil.parseFollowUp(text.get());
        return new FollowUpAddCommand(studentIndex, followUp);
    }
}
