package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.UnarchiveCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.testutil.AddressBookBuilder;

/**
 * Contains tests for {@code UnarchiveCommandParser} and its parsed command's target index.
 */
public class UnarchiveCommandParserTest {

    private final UnarchiveCommandParser parser = new UnarchiveCommandParser();

    @Test
    public void parse_validArgs_returnsUnarchiveCommand() throws ParseException {
        Model model = new ModelManager(
                new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON.withArchived(true)).build(),
                new UserPrefs());
        Model expectedModel = new ModelManager(
                new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build(), new UserPrefs());
        model.setArchiveView(true);
        expectedModel.setArchiveView(true);
        String expectedMessage = String.format(UnarchiveCommand.MESSAGE_UNARCHIVE_PERSON_SUCCESS,
                Messages.format(BENSON.withArchived(true)));

        assertCommandSuccess(parser.parse(" 1 "), model, expectedMessage, expectedModel);
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, UnarchiveCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "a", expectedMessage);
        assertParseFailure(parser, "0", expectedMessage);
        assertParseFailure(parser, "-1", expectedMessage);
        assertParseFailure(parser, "1 2", expectedMessage);
    }
}
