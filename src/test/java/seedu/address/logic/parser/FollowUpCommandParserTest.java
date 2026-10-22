package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FollowUpCommand;
import seedu.address.logic.commands.FollowUpCommand.Operation;
import seedu.address.model.person.FollowUp;

public class FollowUpCommandParserTest {
    private static final String INVALID_ADD_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpCommand.MESSAGE_ADD_USAGE);
    private static final String INVALID_EDIT_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpCommand.MESSAGE_EDIT_USAGE);
    private static final String INVALID_DONE_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, FollowUpCommand.MESSAGE_DONE_USAGE);

    private final FollowUpCommandParser addParser = new FollowUpCommandParser(Operation.ADD);
    private final FollowUpCommandParser editParser = new FollowUpCommandParser(Operation.EDIT);
    private final FollowUpCommandParser doneParser = new FollowUpCommandParser(Operation.DONE);

    private static FollowUpCommand addFirst(String text) {
        return new FollowUpCommand(INDEX_FIRST_PERSON, Operation.ADD, Optional.empty(),
                Optional.of(new FollowUp(text)));
    }

    @Test
    public void parseAdd_validArgs_success() {
        assertParseSuccess(addParser, " 1 text/Send report", addFirst("Send report"));
        assertParseSuccess(addParser, "  1   text/  Send report  ", addFirst("Send report"));
        // Only text/ is a prefix, so words such as and/or stay in the text.
        assertParseSuccess(addParser, " 1 text/Ask about P5 and/or P6 books", addFirst("Ask about P5 and/or P6 books"));
    }

    @Test
    public void parseAdd_invalidArgs_failure() {
        for (String args : new String[] {"", " 1", " text/Send report", " 0 text/Send report",
            " a text/Send report", " 1 2 text/Send report"}) {
            assertParseFailure(addParser, args, INVALID_ADD_FORMAT);
        }
        assertParseFailure(addParser, " 1 text/   ", FollowUp.MESSAGE_TEXT_CONSTRAINTS);
        assertParseFailure(addParser, " 1 text/" + "a".repeat(FollowUp.MAX_TEXT_LENGTH + 1),
                FollowUp.MESSAGE_TEXT_CONSTRAINTS);
        assertParseFailure(addParser, " 1 text/Send report text/Call",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_TEXT));
    }

    @Test
    public void parseEdit_validArgs_success() {
        FollowUpCommand expected = new FollowUpCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.of(INDEX_SECOND_PERSON), Optional.of(new FollowUp("Call on Saturday")));
        assertParseSuccess(editParser, " 1 2 text/Call on Saturday", expected);
        assertParseSuccess(editParser, " 1\t2  text/Call on Saturday ", expected);
    }

    @Test
    public void parseEdit_invalidArgs_failure() {
        for (String args : new String[] {"", " 1 text/Call", " 1 2", " 1 0 text/Call", " 1 2 3 text/Call",
            " 1 b text/Call"}) {
            assertParseFailure(editParser, args, INVALID_EDIT_FORMAT);
        }
        assertParseFailure(editParser, " 1 2 text/", FollowUp.MESSAGE_TEXT_CONSTRAINTS);
    }

    @Test
    public void parseDone_validArgs_success() {
        FollowUpCommand expected = new FollowUpCommand(INDEX_FIRST_PERSON, Operation.DONE,
                Optional.of(INDEX_SECOND_PERSON), Optional.empty());
        assertParseSuccess(doneParser, " 1 2", expected);
        assertParseSuccess(doneParser, "  1   2  ", expected);
    }

    @Test
    public void parseDone_invalidArgs_failure() {
        for (String args : new String[] {"", " 1", " 1 2 3", " 0 1", " 1 0", " a 1", " 1 -2",
            " 1 2 text/Send report"}) {
            assertParseFailure(doneParser, args, INVALID_DONE_FORMAT);
        }
    }
}
