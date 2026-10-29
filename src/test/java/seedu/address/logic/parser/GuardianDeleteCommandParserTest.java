package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.GuardianDeleteCommand;

public class GuardianDeleteCommandParserTest {
    private static final String INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, GuardianDeleteCommand.MESSAGE_USAGE);
    private final GuardianDeleteCommandParser parser = new GuardianDeleteCommandParser();

    @Test
    public void parse_validArgs_success() {
        assertParseSuccess(parser, " 1 ", new GuardianDeleteCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_invalidArgs_failure() {
        for (String args : new String[] {"", "0", "-1", "2147483648", "1 2", "1 name/Janet"}) {
            assertParseFailure(parser, args, INVALID_FORMAT);
        }
    }

}
