package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TEXT;

import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.NoteCommand.Operation;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ProgressNote;

/**
 * Parses progress note commands. Note text is free text: a word such as {@code and/or} inside it is kept, and only
 * {@code date/} and {@code text/} are read as prefixes.
 */
public class NoteCommandParser implements Parser<NoteCommand> {
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private final Operation operation;

    public NoteCommandParser(Operation operation) {
        this.operation = requireNonNull(operation);
    }

    @Override
    public NoteCommand parse(String args) throws ParseException {
        requireNonNull(args);
        return switch (operation) {
            case ADD -> parseAdd(args);
            case DELETE -> parseDelete(args);
        };
    }

    private static NoteCommand parseAdd(String args) throws ParseException {
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_ADD_USAGE);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_DATE, PREFIX_TEXT);
        Index studentIndex;
        try {
            studentIndex = ParserUtil.parseIndex(arguments.getPreamble().trim());
        } catch (ParseException e) {
            throw new ParseException(formatError, e);
        }
        Optional<String> date = arguments.getValue(PREFIX_DATE);
        Optional<String> text = arguments.getValue(PREFIX_TEXT);
        if (date.isEmpty() || text.isEmpty()) {
            throw new ParseException(formatError);
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_DATE, PREFIX_TEXT);
        ProgressNote note = ParserUtil.parseProgressNote(date.get(), text.get(), LocalDate.now());
        return new NoteCommand(studentIndex, Operation.ADD, Optional.empty(), Optional.of(note));
    }

    private static NoteCommand parseDelete(String args) throws ParseException {
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_DELETE_USAGE);
        String[] indexes = WHITESPACE.split(args.trim());
        if (indexes.length != 2) {
            throw new ParseException(formatError);
        }
        try {
            return new NoteCommand(ParserUtil.parseIndex(indexes[0]), Operation.DELETE,
                    Optional.of(ParserUtil.parseIndex(indexes[1])), Optional.empty());
        } catch (ParseException e) {
            throw new ParseException(formatError, e);
        }
    }
}
