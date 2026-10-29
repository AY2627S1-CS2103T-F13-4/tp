package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FollowUpDoneCommand;

public class FollowUpDoneCommandParserTest {
    private static final String INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpDoneCommand.MESSAGE_USAGE);

    private final FollowUpDoneCommandParser parser = new FollowUpDoneCommandParser();

    @Test
    public void parseDone_validArgs_success() {
        FollowUpDoneCommand expected = new FollowUpDoneCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON);
        assertParseSuccess(parser, " 1 2", expected);
        assertParseSuccess(parser, "  1   2  ", expected);
    }

    @Test
    public void parseDone_invalidArgs_failure() {
        for (String args : new String[] {"", " 1", " 1 2 3", " 0 1", " 1 0", " a 1", " 1 -2",
            " 1 2 text/Send report"}) {
            assertParseFailure(parser, args, INVALID_FORMAT);
        }
    }
}
