package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.NoteEditCommand;
import seedu.address.logic.commands.NoteEditCommand.EditNoteDescriptor;
import seedu.address.model.person.ProgressNote;

public class NoteEditCommandParserTest {
    private static final String INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, NoteEditCommand.MESSAGE_USAGE);
    private static final LocalDate DATE = LocalDate.of(2026, 10, 15);
    private final NoteEditCommandParser parser = new NoteEditCommandParser();

    private static NoteEditCommand edit(LocalDate date, String text) {
        EditNoteDescriptor descriptor = new EditNoteDescriptor();
        if (date != null) {
            descriptor.setDate(date);
        }
        if (text != null) {
            descriptor.setText(text);
        }
        return new NoteEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, descriptor);
    }

    @Test
    public void parse_partialAndFullEdits_success() {
        assertParseSuccess(parser, " 1 2 date/2026-10-15", edit(DATE, null));
        assertParseSuccess(parser, " 1 2 text/Finished fractions", edit(null, "Finished fractions"));
        assertParseSuccess(parser, " 1  2 date/ 2026-10-15  text/  Finished fractions  ",
                edit(DATE, "Finished fractions"));
    }

    @Test
    public void parse_prefixesAfterText_keptAsText() {
        assertParseSuccess(parser, " 1 2 text/Revise and/or redo Q3 date/2026-10-15 text/Workbook",
                edit(null, "Revise and/or redo Q3 date/2026-10-15 text/Workbook"));
        assertParseSuccess(parser, " 1 2 date/2026-10-15 text/Review date/time and text/workbook",
                edit(DATE, "Review date/time and text/workbook"));
    }

    @Test
    public void parse_invalidIndexes_failure() {
        for (String input : List.of("", "1", "1 2 3 text/Note", "0 2 text/Note", "1 -1 text/Note",
                "a 2 text/Note", "1 2147483648 text/Note", "1 dte/2026-10-15 text/Note")) {
            assertParseFailure(parser, input, INVALID_FORMAT);
        }
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_noFields_failure() {
        assertParseFailure(parser, " 1 2", NoteEditCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void parse_invalidDates_failure() {
        for (String date : List.of("", "2026-02-30", "15-10-2026")) {
            assertParseFailure(parser, " 1 2 date/" + date, ProgressNote.MESSAGE_DATE_CONSTRAINTS);
        }
        assertParseFailure(parser, " 1 2 date/" + LocalDate.now().plusDays(1), ParserUtil.MESSAGE_FUTURE_NOTE_DATE);
        assertParseFailure(parser, " 1 2 date/1999-12-31", ParserUtil.MESSAGE_EARLY_NOTE_DATE);
    }

    @Test
    public void parse_dateBoundaries_success() {
        assertParseSuccess(parser, " 1 2 date/2000-01-01", edit(ParserUtil.EARLIEST_NOTE_DATE, null));
        LocalDate today = LocalDate.now();
        assertParseSuccess(parser, " 1 2 date/" + today, edit(today, null));
    }

    @Test
    public void parse_invalidText_failure() {
        for (String text : List.of("", " ", "a".repeat(501), "First\nSecond", "First\tSecond", "First\u2028Second")) {
            assertParseFailure(parser, " 1 2 text/" + text, ProgressNote.MESSAGE_TEXT_CONSTRAINTS);
        }
    }

    @Test
    public void parse_maximumTextLength_countsUnicodeCodePoints() {
        String text = "\ud83d\ude00".repeat(500);
        assertParseSuccess(parser, " 1 2 text/" + text, edit(null, text));
    }

    @Test
    public void parse_multipleErrors_checksIndexesThenDuplicatePrefixesThenValues() {
        assertParseFailure(parser, " 0 2 date/invalid date/2026-10-15", INVALID_FORMAT);
        assertParseFailure(parser, " 1 2 date/invalid date/2026-10-15",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_DATE));
    }
}
