package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TEXT;

import java.time.LocalDate;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NoteAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ProgressNote;

/**
 * Parses dated progress notes. Note text is free text: everything after {@code text/} is the text, including words
 * such as {@code and/or}, {@code date/} or {@code text/}, so {@code text/} must come last.
 */
public class NoteAddCommandParser implements Parser<NoteAddCommand> {

    @Override
    public NoteAddCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteAddCommand.MESSAGE_USAGE);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenizeWithTrailingValue(args, PREFIX_TEXT, PREFIX_DATE);
        Index studentIndex;
        try {
            studentIndex = ParserUtil.parseIndex(arguments.getPreamble());
        } catch (ParseException e) {
            throw new ParseException(formatError, e);
        }
        Optional<String> date = arguments.getValue(PREFIX_DATE);
        Optional<String> text = arguments.getValue(PREFIX_TEXT);
        if (date.isEmpty() || text.isEmpty()) {
            throw new ParseException(formatError);
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_DATE);
        ProgressNote note = ParserUtil.parseProgressNote(date.get(), text.get(), LocalDate.now());
        return new NoteAddCommand(studentIndex, note);
    }
}
