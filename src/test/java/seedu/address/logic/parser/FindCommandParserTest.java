package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.SchoolingLevelContainsKeywordPredicate;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_levelPrefix_returnsLevelFindCommand() {
        FindCommand expected = new FindCommand(new SchoolingLevelContainsKeywordPredicate("Primary 5"));
        assertParseSuccess(parser, " l/Primary 5", expected);
        assertParseSuccess(parser, "   l/  Primary   5  ", new FindCommand(
                new SchoolingLevelContainsKeywordPredicate("Primary   5")));
    }

    @Test
    public void parse_invalidLevelSearch_throwsParseException() {
        String invalid = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " l/", invalid);
        assertParseFailure(parser, " l/   ", invalid);
        assertParseFailure(parser, " alice l/Primary", invalid);
        assertParseFailure(parser, " l/Primary l/Secondary",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LEVEL));
    }

    @Test
    public void parse_levelPrefixInsideWord_treatedAsNameKeyword() {
        assertParseSuccess(parser, "bil/l", new FindCommand(new NameContainsKeywordsPredicate(List.of("bil/l"))));
    }

}
