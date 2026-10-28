package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.NoteCommand.Operation;
import seedu.address.model.person.ProgressNote;

public class NoteCommandParserTest {
    private static final String INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_ADD_USAGE);
    private static final String INVALID_DELETE_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteCommand.MESSAGE_DELETE_USAGE);
    private static final LocalDate DATE = LocalDate.of(2026, 10, 15);

    private final NoteCommandParser parser = new NoteCommandParser(Operation.ADD);
    private final NoteCommandParser deleteParser = new NoteCommandParser(Operation.DELETE);

    private static NoteCommand addFirst(LocalDate date, String text) {
        return new NoteCommand(INDEX_FIRST_PERSON, Operation.ADD, Optional.empty(),
                Optional.of(new ProgressNote(date, text)));
    }

    @Test
    public void parseDelete_validArgs_success() {
        NoteCommand expected = new NoteCommand(INDEX_FIRST_PERSON, Operation.DELETE,
                Optional.of(INDEX_SECOND_PERSON), Optional.empty());
        assertParseSuccess(deleteParser, " 1 2", expected);
        assertParseSuccess(deleteParser, "  1   2  ", expected);
    }

    @Test
    public void parseDelete_invalidArgs_failure() {
        for (String args : new String[] {"", " 1", " 1 2 3", " 0 1", " 1 0", " a 1", " 1 -2",
            " 1 2 date/2026-10-15", " 1 text/Fractions"}) {
            assertParseFailure(deleteParser, args, INVALID_DELETE_FORMAT);
        }
    }

    @Test
    public void parse_validArgs_success() {
        NoteCommand expected = addFirst(DATE, "Finished fractions");
        assertParseSuccess(parser, " 1 date/2026-10-15 text/Finished fractions", expected);
        // extra spaces trimmed
        assertParseSuccess(parser, " 1  date/ 2026-10-15  text/  Finished fractions  ", expected);
    }

    @Test
    public void parse_todayAllowed() {
        LocalDate today = LocalDate.now();
        assertParseSuccess(parser, " 1 date/" + ProgressNote.formatDate(today) + " text/Quiz",
                addFirst(today, "Quiz"));
    }

    @Test
    public void parse_wordWithSlashInText_keptAsText() {
        assertParseSuccess(parser, " 1 date/2026-10-15 text/Revise and/or redo Q3 w/o a calculator",
                addFirst(DATE, "Revise and/or redo Q3 w/o a calculator"));
    }

    @Test
    public void parse_prefixWordsInText_keptAsText() {
        assertParseSuccess(parser,
                " 1 date/2026-10-15 text/Practised the essay; next session update date/time of mock exam",
                addFirst(DATE, "Practised the essay; next session update date/time of mock exam"));
        assertParseSuccess(parser, " 1 date/2026-10-15 text/Bring the text/workbook bundle",
                addFirst(DATE, "Bring the text/workbook bundle"));
    }

    @Test
    public void parse_dateAfterText_treatedAsText() {
        // date/ after text/ is part of the text, so the note has no date
        assertParseFailure(parser, " 1 text/Fractions date/2026-10-15", INVALID_FORMAT);
        assertParseSuccess(parser, " 1 date/2026-10-15 text/Fractions date/2026-10-16",
                addFirst(DATE, "Fractions date/2026-10-16"));
    }

    @Test
    public void parse_missingParts_failure() {
        assertParseFailure(parser, "", INVALID_FORMAT);
        assertParseFailure(parser, " date/2026-10-15 text/Fractions", INVALID_FORMAT);
        assertParseFailure(parser, " 1 text/Fractions", INVALID_FORMAT);
        assertParseFailure(parser, " 1 date/2026-10-15", INVALID_FORMAT);
        assertParseFailure(parser, " 0 date/2026-10-15 text/Fractions", INVALID_FORMAT);
        assertParseFailure(parser, " 1 2 date/2026-10-15 text/Fractions", INVALID_FORMAT);
        // a mistyped prefix is not silently dropped
        assertParseFailure(parser, " 1 dte/2026-10-15 text/Fractions", INVALID_FORMAT);
    }

    @Test
    public void parse_invalidValues_failure() {
        assertParseFailure(parser, " 1 date/2026-02-30 text/Fractions", ProgressNote.MESSAGE_DATE_CONSTRAINTS);
        assertParseFailure(parser, " 1 date/15-10-2026 text/Fractions", ProgressNote.MESSAGE_DATE_CONSTRAINTS);
        assertParseFailure(parser, " 1 date/ text/Fractions", ProgressNote.MESSAGE_DATE_CONSTRAINTS);
        assertParseFailure(parser, " 1 date/2026-10-15 text/ ", ProgressNote.MESSAGE_TEXT_CONSTRAINTS);
        assertParseFailure(parser, " 1 date/2026-10-15 text/" + "a".repeat(501),
                ProgressNote.MESSAGE_TEXT_CONSTRAINTS);
    }

    @Test
    public void parse_futureDate_failure() {
        String tomorrow = ProgressNote.formatDate(LocalDate.now().plusDays(1));
        assertParseFailure(parser, " 1 date/" + tomorrow + " text/Quiz", ParserUtil.MESSAGE_FUTURE_NOTE_DATE);
    }

    @Test
    public void parse_repeatedPrefix_failure() {
        assertParseFailure(parser, " 1 date/2026-10-15 date/2026-10-16 text/Fractions",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_DATE));
    }
}
