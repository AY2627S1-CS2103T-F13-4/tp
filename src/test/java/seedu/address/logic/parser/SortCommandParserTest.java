package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand;
import seedu.address.model.person.RateSortOrder;

public class SortCommandParserTest {

    private static final String INVALID_FORMAT = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
            SortCommand.MESSAGE_USAGE);

    private final SortCommandParser parser = new SortCommandParser();

    @Test
    public void parse_validArgs_returnsSortCommand() {
        assertParseSuccess(parser, " rate asc", new SortCommand(RateSortOrder.ASCENDING));
        assertParseSuccess(parser, "  RATE   Desc ", new SortCommand(RateSortOrder.DESCENDING));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, "", INVALID_FORMAT);
        assertParseFailure(parser, " rate", INVALID_FORMAT);
        assertParseFailure(parser, " rate up", INVALID_FORMAT);
        assertParseFailure(parser, " name asc", INVALID_FORMAT);
        assertParseFailure(parser, " rate asc extra", INVALID_FORMAT);
    }
}
