package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FollowUpAddCommand;
import seedu.address.model.person.FollowUp;

public class FollowUpAddCommandParserTest {
    private static final String INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpAddCommand.MESSAGE_USAGE);

    private final FollowUpAddCommandParser parser = new FollowUpAddCommandParser();

    private static FollowUpAddCommand addFirst(String text) {
        return new FollowUpAddCommand(INDEX_FIRST_PERSON, new FollowUp(text));
    }

    @Test
    public void parseAdd_validArgs_success() {
        assertParseSuccess(parser, " 1 text/Send report", addFirst("Send report"));
        assertParseSuccess(parser, "  1   text/  Send report  ", addFirst("Send report"));
        // Only text/ is a prefix, so words such as and/or stay in the text.
        assertParseSuccess(parser, " 1 text/Ask about P5 and/or P6 books", addFirst("Ask about P5 and/or P6 books"));
    }

    @Test
    public void parseAdd_prefixWordsInText_keptAsText() {
        assertParseSuccess(parser, " 1 text/Ask mum to buy the text/workbook bundle",
                addFirst("Ask mum to buy the text/workbook bundle"));
        assertParseSuccess(parser, " 1 text/Confirm date/time of mock exam",
                addFirst("Confirm date/time of mock exam"));
    }

    @Test
    public void parseAdd_invalidArgs_failure() {
        for (String args : new String[] {"", " 1", " text/Send report", " 0 text/Send report",
            " a text/Send report", " 1 2 text/Send report"}) {
            assertParseFailure(parser, args, INVALID_FORMAT);
        }
        assertParseFailure(parser, " 1 text/   ", FollowUp.MESSAGE_TEXT_CONSTRAINTS);
        assertParseFailure(parser, " 1 text/" + "a".repeat(FollowUp.MAX_TEXT_LENGTH + 1),
                FollowUp.MESSAGE_TEXT_CONSTRAINTS);
    }
}
