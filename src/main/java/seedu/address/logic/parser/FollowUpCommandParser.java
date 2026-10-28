package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TEXT;

import java.util.Optional;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.FollowUpCommand.Operation;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FollowUp;

/**
 * Parses guardian follow-up commands. Follow-up text is free text: everything after {@code text/} is the text,
 * including words such as {@code and/or} or {@code text/}.
 */
public class FollowUpCommandParser implements Parser<FollowUpCommand> {
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private final Operation operation;

    public FollowUpCommandParser(Operation operation) {
        this.operation = requireNonNull(operation);
    }

    @Override
    public FollowUpCommand parse(String args) throws ParseException {
        requireNonNull(args);
        return switch (operation) {
            case ADD -> parseWithText(args, Operation.ADD, FollowUpCommand.MESSAGE_ADD_USAGE, 1);
            case EDIT -> parseWithText(args, Operation.EDIT, FollowUpCommand.MESSAGE_EDIT_USAGE, 2);
            case DONE -> parseDone(args);
        };
    }

    /** Parses {@code indexCount} indexes followed by {@code text/TEXT}, as used by adding and editing. */
    private static FollowUpCommand parseWithText(String args, Operation operation, String usage, int indexCount)
            throws ParseException {
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenizeWithTrailingValue(args, PREFIX_TEXT);
        Index[] indexes = parseIndexes(arguments.getPreamble(), indexCount, formatError);
        Optional<String> text = arguments.getValue(PREFIX_TEXT);
        if (text.isEmpty()) {
            throw new ParseException(formatError);
        }
        FollowUp followUp = ParserUtil.parseFollowUp(text.get());
        Optional<Index> followUpIndex = indexCount == 2 ? Optional.of(indexes[1]) : Optional.empty();
        return new FollowUpCommand(indexes[0], operation, followUpIndex, Optional.of(followUp));
    }

    private static FollowUpCommand parseDone(String args) throws ParseException {
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpCommand.MESSAGE_DONE_USAGE);
        Index[] indexes = parseIndexes(args, 2, formatError);
        return new FollowUpCommand(indexes[0], Operation.DONE, Optional.of(indexes[1]), Optional.empty());
    }

    /** Parses exactly {@code count} positive indexes separated by whitespace, or reports {@code formatError}. */
    private static Index[] parseIndexes(String value, int count, String formatError) throws ParseException {
        String trimmed = value.trim();
        String[] parts = trimmed.isEmpty() ? new String[0] : WHITESPACE.split(trimmed);
        if (parts.length != count) {
            throw new ParseException(formatError);
        }
        Index[] indexes = new Index[count];
        try {
            for (int i = 0; i < count; i++) {
                indexes[i] = ParserUtil.parseIndex(parts[i]);
            }
        } catch (ParseException e) {
            throw new ParseException(formatError, e);
        }
        return indexes;
    }
}
