package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindCommand;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.LessonMatchesPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.SchoolingLevelContainsKeywordPredicate;
import seedu.address.model.person.StudentLessonMatchesPredicate;
import seedu.address.testutil.PersonBuilder;

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
        assertParseSuccess(parser, " level/Primary 5", expected);
        assertParseSuccess(parser, "   level/  Primary   5  ", new FindCommand(
                new SchoolingLevelContainsKeywordPredicate("Primary   5")));
    }

    @Test
    public void parse_levelOnly_keepsStudentWithoutLessons() throws Exception {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder().withSchoolingLevel(new SchoolingLevel("Primary 5")).build());

        parser.parse(" level/primary").execute(model);

        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void parse_invalidLevelSearch_throwsParseException() {
        String invalid = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " level/", invalid);
        assertParseFailure(parser, " level/   ", invalid);
        assertParseFailure(parser, " alice level/Primary", invalid);
        assertParseFailure(parser, " level/Primary level/Secondary",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LEVEL));
    }

    @Test
    public void parse_levelPrefixInsideWord_treatedAsNameKeyword() {
        assertParseSuccess(parser, "bil/l", new FindCommand(new NameContainsKeywordsPredicate(List.of("bil/l"))));
    }

    @Test
    public void parse_lessonCriteria_acceptsSubjectDayAndLevel() {
        LessonMatchesPredicate wednesdayMath = new LessonMatchesPredicate(
                Optional.of("Math"), Optional.of(DayOfWeek.WEDNESDAY));
        assertParseSuccess(parser, " subject/math day/wed",
                new FindCommand(new StudentLessonMatchesPredicate(Optional.empty(), wednesdayMath), wednesdayMath));
        assertParseSuccess(parser, " day/Wednesday subject/Math level/Secondary",
                new FindCommand(new StudentLessonMatchesPredicate(Optional.of("Secondary"), wednesdayMath),
                        wednesdayMath));

        LessonMatchesPredicate monday = new LessonMatchesPredicate(Optional.empty(), Optional.of(DayOfWeek.MONDAY));
        assertParseSuccess(parser, " day/mon",
                new FindCommand(new StudentLessonMatchesPredicate(Optional.empty(), monday), monday));
    }

    @Test
    public void parse_invalidLessonCriteria_throwsParseException() {
        String invalid = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " subject/", invalid);
        assertParseFailure(parser, " day/Funday", invalid);
        assertParseFailure(parser, " level/Primary subject/", invalid);
        assertParseFailure(parser, " alice subject/Math", invalid);
        assertParseFailure(parser, " subject/Math subject/English",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SUBJECT));
    }

}
