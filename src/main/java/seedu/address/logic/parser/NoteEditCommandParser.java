package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TEXT;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NoteEditCommand;
import seedu.address.logic.commands.NoteEditCommand.EditNoteDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses note edits, treating everything after the first {@code text/} as free text, as in note-add. */
public class NoteEditCommandParser implements Parser<NoteEditCommand> {
    private static final int REQUIRED_INDEX_COUNT = 2;

    @Override
    public NoteEditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteEditCommand.MESSAGE_USAGE);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenizeWithTrailingValue(args, PREFIX_TEXT, PREFIX_DATE);
        List<Index> indexes;
        try {
            indexes = ParserUtil.parseIndexes(arguments.getPreamble(), REQUIRED_INDEX_COUNT);
        } catch (ParseException e) {
            throw new ParseException(formatError, e);
        }
        Optional<String> date = arguments.getValue(PREFIX_DATE);
        Optional<String> text = arguments.getValue(PREFIX_TEXT);
        if (date.isEmpty() && text.isEmpty()) {
            throw new ParseException(NoteEditCommand.MESSAGE_NOT_EDITED);
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_DATE);
        EditNoteDescriptor descriptor = new EditNoteDescriptor();
        if (date.isPresent()) {
            descriptor.setDate(ParserUtil.parseProgressNoteDate(date.get(), LocalDate.now()));
        }
        if (text.isPresent()) {
            descriptor.setText(ParserUtil.parseProgressNoteText(text.get()));
        }
        return new NoteEditCommand(indexes.getFirst(), indexes.getLast(), descriptor);
    }
}
