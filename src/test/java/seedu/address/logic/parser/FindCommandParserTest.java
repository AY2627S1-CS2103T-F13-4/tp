package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GUARDIAN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindCommand;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonMatchesPredicate;
import seedu.address.model.person.Name;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Phone;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.SchoolingLevelContainsKeywordPredicate;
import seedu.address.model.person.Student;
import seedu.address.model.person.StudentContactMatchesPredicate;
import seedu.address.model.person.StudentLessonMatchesPredicate;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
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

    @Test
    public void parse_contactSearch_matchesSubstringsAndPreservesBareNameSearch() throws Exception {
        Model model = new ModelManager();
        Student alex = new PersonBuilder().withName("Alex Tan").withGuardian(
                new Guardian(new Name("Janet Lee"), new Phone("91234567"))).build();
        Student alexander = new PersonBuilder().withName("Alexander Tan").withPhone("91234567").build();
        model.addPerson(alex);
        model.addPerson(alexander);

        parser.parse("alex").execute(model);
        assertEquals(List.of(alex), model.getFilteredPersonList());
        parser.parse("name/aLeX").execute(model);
        assertEquals(List.of(alex, alexander), model.getFilteredPersonList());
        parser.parse("guardian/AnEt").execute(model);
        assertEquals(List.of(alex), model.getFilteredPersonList());
        parser.parse("guardian/1234").execute(model);
        assertEquals(List.of(alex), model.getFilteredPersonList());
        parser.parse("name/alex guardian/missing").execute(model);
        assertEquals(List.of(), model.getFilteredPersonList());
        parser.parse("name/alexander guardian/janet").execute(model);
        assertEquals(List.of(), model.getFilteredPersonList());
        parser.parse("name/lex tan").execute(model);
        assertEquals(List.of(alex), model.getFilteredPersonList());
    }

    @Test
    public void parse_combinedContactSearch_filtersSameLessonAndCalendar() throws Exception {
        Model model = new ModelManager();
        Lesson tuesdayMath = lesson("Math", DayOfWeek.TUESDAY);
        Lesson mondayEnglish = lesson("English", DayOfWeek.MONDAY);
        Lesson mondayMath = lesson("Math", DayOfWeek.MONDAY);
        Student alex = new PersonBuilder().withName("Alex Tan")
                .withGuardian(new Guardian(new Name("Janet Lee"), new Phone("91234567")))
                .withSchoolingLevel(new SchoolingLevel("Secondary 3"))
                .withLessons(tuesdayMath, mondayEnglish).build();
        model.addPerson(alex);

        parser.parse("name/alex guardian/janet level/secondary subject/math day/mon").execute(model);
        assertEquals(List.of(), model.getFilteredPersonList());

        Student matching = new PersonBuilder(alex).withLessons(tuesdayMath, mondayMath).build();
        model.setPerson(alex, matching);
        parser.parse("day/Monday\tguardian/Janet\nname/ALEX level/secondary subject/math").execute(model);
        assertEquals(List.of(matching), model.getFilteredPersonList());
        assertFalse(model.getLessonPredicate().getValue().test(tuesdayMath));
        assertTrue(model.getLessonPredicate().getValue().test(mondayMath));

        parser.parse("name/alex guardian/janet level/primary").execute(model);
        assertEquals(List.of(), model.getFilteredPersonList());
        parser.parse("name/alex").execute(model);
        assertTrue(model.getLessonPredicate().getValue().test(tuesdayMath));
    }

    @Test
    public void parse_contactCriteria_returnsEquivalentCommandsRegardlessOfCaseOrOrder() {
        FindCommand expected = new FindCommand(new StudentContactMatchesPredicate(
                Optional.of("alex"), Optional.of("janet"), Model.PREDICATE_SHOW_ALL_PERSONS));
        assertParseSuccess(parser, "name/ALEX guardian/Janet", expected);
        assertParseSuccess(parser, "guardian/JANET name/alex", expected);
    }

    @Test
    public void parse_invalidContactCriteria_throwsParseException() {
        String invalid = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        for (String input : List.of("name/", "guardian/  ", "name/alex guardian/", "alex name/alex",
                "alex guardian/janet", "guardian/janet day/Funday", "name/alex\tguardian/")) {
            assertParseFailure(parser, input, invalid);
        }
        assertParseFailure(parser, "name/alex name/bob",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, "guardian/janet\tguardian/john",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_GUARDIAN));
        assertParseSuccess(parser, "surname/alex", new FindCommand(
                new NameContainsKeywordsPredicate(List.of("surname/alex"))));
    }

    private static Lesson lesson(String subject, DayOfWeek day) {
        return new Lesson(new Subject(subject),
                new WeeklyLessonSlot(day, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                new HourlyRate(new BigDecimal("45")));
    }

}
