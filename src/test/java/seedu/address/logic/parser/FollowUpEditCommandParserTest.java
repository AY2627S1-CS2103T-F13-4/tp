package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FollowUpEditCommand;
import seedu.address.model.person.FollowUp;

public class FollowUpEditCommandParserTest {
    private static final String INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpEditCommand.MESSAGE_USAGE);

    private final FollowUpEditCommandParser parser = new FollowUpEditCommandParser();

    @Test
    public void parseEdit_validArgs_success() {
        FollowUpEditCommand expected = new FollowUpEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                new FollowUp("Call on Saturday"));
        assertParseSuccess(parser, " 1 2 text/Call on Saturday", expected);
        assertParseSuccess(parser, " 1\t2  text/Call on Saturday ", expected);
        assertParseSuccess(parser, " 1 2 text/Call  on   Saturday", expected);
    }

    @Test
    public void parseEdit_prefixWordsInText_keptAsText() {
        FollowUpEditCommand expected = new FollowUpEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                new FollowUp("Buy the text/workbook; date/time TBC"));
        assertParseSuccess(parser, " 1 2 text/Buy the text/workbook; date/time TBC", expected);
    }

    @Test
    public void parseEdit_invalidArgs_failure() {
        for (String args : new String[] {"", " 1 text/Call", " 1 2", " 1 0 text/Call", " 1 2 3 text/Call",
            " 1 b text/Call"}) {
            assertParseFailure(parser, args, INVALID_FORMAT);
        }
        assertParseFailure(parser, " 1 2 text/", FollowUp.MESSAGE_TEXT_CONSTRAINTS);
    }
}
