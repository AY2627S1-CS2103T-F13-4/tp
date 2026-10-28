package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.LessonDeleteCommand;

public class LessonDeleteCommandParserTest {
    private static final String USAGE = LessonDeleteCommand.MESSAGE_USAGE;

    private final LessonDeleteCommandParser parser = new LessonDeleteCommandParser();

    private static String invalidFormat(String usage) {
        return String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage);
    }

    @Test
    public void parseDelete_success() {
        assertParseSuccess(parser, "1 2", new LessonDeleteCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON));
    }

    @Test
    public void parseDelete_invalid_failure() {
        assertParseFailure(parser, "", invalidFormat(USAGE));
        assertParseFailure(parser, "1", invalidFormat(USAGE));
        assertParseFailure(parser, "1 2 3", invalidFormat(USAGE));
        assertParseFailure(parser, "1 2 rate/50", invalidFormat(USAGE));
        assertParseFailure(parser, "-1 2", invalidFormat(USAGE));
    }

    @Test
    public void parse_lessonFields_failure() {
        assertParseFailure(parser, "1 2 mode/virtual", invalidFormat(USAGE));
    }

    @Test
    public void parse_whitespaceBetweenIndexes_success() {
        assertParseSuccess(parser, " \t1\t 2\t ",
                new LessonDeleteCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON));
    }
}
