package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.GuardianAddCommand;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

public class GuardianAddCommandParserTest {
    private static final String INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, GuardianAddCommand.MESSAGE_USAGE);
    private final GuardianAddCommandParser parser = new GuardianAddCommandParser();

    @Test
    public void parse_validArgs_success() {
        Guardian contact = new Guardian(new Name("Janet Tan"), new Phone("91234567"));
        GuardianAddCommand expected = new GuardianAddCommand(INDEX_FIRST_PERSON, contact);
        assertParseSuccess(parser, "1 name/Janet Tan phone/91234567", expected);
        assertParseSuccess(parser, " 1 phone/91234567 name/ Janet Tan ", expected);
    }

    @Test
    public void parse_missingOrUnknownFields_failure() {
        for (String args : new String[] {"", "0 name/Janet phone/91234567", "1 name/Janet", "1 phone/91234567",
            "1 name/Janet phone/91234567 rate/50", "1 name/Janet phone/91234567 x1/y"}) {
            assertParseFailure(parser, args, INVALID_FORMAT);
        }
    }

    @Test
    public void parse_invalidOrRepeatedFields_failure() {
        assertParseFailure(parser, "1 name/ phone/91234567", Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 name/Janet phone/abc", Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 name/Janet name/John phone/91234567",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NAME));
        assertParseFailure(parser, "1 name/Janet phone/91234567 phone/87654321",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_PHONE));
    }

}
