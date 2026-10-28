package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TEXT;

import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.FollowUpEditCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUp;

/**
 * Parses follow-up edits, preserving words such as {@code and/or} as free text.
 */
public class FollowUpEditCommandParser implements Parser<FollowUpEditCommand> {
    private static final int REQUIRED_INDEX_COUNT = 2;

    @Override
    public FollowUpEditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpEditCommand.MESSAGE_USAGE);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_TEXT);
        List<Index> indexes;
        try {
            indexes = ParserUtil.parseIndexes(arguments.getPreamble(), REQUIRED_INDEX_COUNT);
        } catch (ParseException e) {
            throw new ParseException(formatError, e);
        }
        Optional<String> text = arguments.getValue(PREFIX_TEXT);
        if (text.isEmpty()) {
            throw new ParseException(formatError);
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_TEXT);
        FollowUp followUp = ParserUtil.parseFollowUp(text.get());
        return new FollowUpEditCommand(indexes.getFirst(), indexes.getLast(), followUp);
    }
}
