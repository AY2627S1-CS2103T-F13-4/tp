package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.GuardianEditCommand;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.testutil.EditGuardianDescriptorBuilder;

public class GuardianEditCommandParserTest {
    private static final String INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, GuardianEditCommand.MESSAGE_USAGE);
    private final GuardianEditCommandParser parser = new GuardianEditCommandParser();

    @Test
    public void parse_partialEdits_success() {
        assertParseSuccess(parser, "1 phone/98765432", new GuardianEditCommand(INDEX_FIRST_PERSON,
                new EditGuardianDescriptorBuilder().withPhone(new Phone("98765432")).build()));
        assertParseSuccess(parser, "1 name/John Tan", new GuardianEditCommand(INDEX_FIRST_PERSON,
                new EditGuardianDescriptorBuilder().withName(new Name("John Tan")).build()));
        assertParseSuccess(parser, "1 name/John Tan phone/98765432", new GuardianEditCommand(INDEX_FIRST_PERSON,
                new EditGuardianDescriptorBuilder().withName(new Name("John Tan"))
                        .withPhone(new Phone("98765432")).build()));
    }

    @Test
    public void parse_invalidOrRepeatedFields_failure() {
        assertParseFailure(parser, "1", INVALID_FORMAT);
        assertParseFailure(parser, "1 rate/50", INVALID_FORMAT);
        assertParseFailure(parser, "1 name/John rate/50", INVALID_FORMAT);
        assertParseFailure(parser, "1 name/", Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 phone/", Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 name/Janet name/John",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NAME));
        assertParseFailure(parser, "1 phone/91234567 phone/87654321",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_PHONE));
    }

}
