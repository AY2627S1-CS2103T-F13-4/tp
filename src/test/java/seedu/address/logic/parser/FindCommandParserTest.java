package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GUARDIAN;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SCHOOL;
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
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.WeekCommand;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonMatchesPredicate;
import seedu.address.model.person.Name;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Phone;
import seedu.address.model.person.School;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.person.StudentSearchPredicate;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.PersonBuilder;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_contactCriteria_preservesBareNameMatchingAndGuardianPhoneSearch() throws Exception {
        Model model = new ModelManager();
        Student alex = new PersonBuilder().withName("Alex Tan").withGuardian(
                new Guardian(new Name("Janet Lee"), new Phone("91234567"))).build();
        Student alexander = new PersonBuilder().withName("Alexander Tan").withPhone("91234567").build();
        model.addPerson(alex);
        model.addPerson(alexander);
        parser.parse("alex").execute(model);
        assertEquals(List.of(alex), model.getFilteredPersonList());
        parser.parse("alex alexander").execute(model);
        assertEquals(List.of(alex, alexander), model.getFilteredPersonList());
        parser.parse("name/aLeX").execute(model);
        assertEquals(List.of(alex, alexander), model.getFilteredPersonList());
        parser.parse("guardian/ANET").execute(model);
        assertEquals(List.of(alex), model.getFilteredPersonList());
        parser.parse("guardian/1234").execute(model);
        assertEquals(List.of(alex), model.getFilteredPersonList());
        parser.parse("name/alexander guardian/janet").execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
        parser.parse("name/lex tan").execute(model);
        assertEquals(List.of(alex), model.getFilteredPersonList());
    }

    @Test
    public void parse_allCriteria_usesOneStudentPredicateAndSameLessonForCalendar() throws Exception {
        Model model = new ModelManager();
        Lesson tuesdayMath = lesson("Math", DayOfWeek.TUESDAY);
        Lesson mondayEnglish = lesson("English", DayOfWeek.MONDAY);
        Lesson mondayMath = lesson("Math", DayOfWeek.MONDAY);
        Student alex = new PersonBuilder().withName("Alex Tan").withSchool(new School("Raffles Institution"))
                .withGuardian(new Guardian(new Name("Janet Lee"), new Phone("91234567")))
                .withSchoolingLevel(new SchoolingLevel("Secondary 3"))
                .withLessons(tuesdayMath, mondayEnglish).build();
        model.addPerson(alex);
        String query = "name/alex guardian/janet sch/raffles level/secondary subject/math day/mon";
        parser.parse(query).execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());

        Student matching = new PersonBuilder(alex).withLessons(tuesdayMath, mondayEnglish, mondayMath).build();
        Student wrongSchool = new PersonBuilder(matching).withName("Alexander Tan")
                .withSchool(new School("Dunman High")).build();
        Student noGuardian = new PersonBuilder(matching).withName("Alex Lee").withGuardian(null).build();
        model.setPerson(alex, matching);
        model.addPerson(wrongSchool);
        model.addPerson(noGuardian);
        FindCommand expected = new FindCommand(new StudentSearchPredicate(Optional.of("alex"), Optional.of("janet"),
                Optional.of("raffles"), Optional.of("secondary"), Optional.of(new LessonMatchesPredicate(
                        Optional.of("math"), Optional.of(DayOfWeek.MONDAY)))),
                new LessonMatchesPredicate(Optional.of("math"), Optional.of(DayOfWeek.MONDAY)));
        assertParseSuccess(parser, query, expected);
        assertParseSuccess(parser, "day/MONDAY\tguardian/JANET sch/RAFFLES\nname/ALEX level/SECONDARY subject/MATH",
                expected);
        parser.parse(query).execute(model);
        new WeekCommand().execute(model);
        assertEquals(List.of(matching), model.getFilteredPersonList());
        assertFalse(model.getLessonPredicate().getValue().test(tuesdayMath));
        assertFalse(model.getLessonPredicate().getValue().test(mondayEnglish));
        assertTrue(model.getLessonPredicate().getValue().test(mondayMath));
        parser.parse("name/alex guardian/janet sch/raffles").execute(model);
        assertTrue(model.getLessonPredicate().getValue().test(tuesdayMath));
    }

    @Test
    public void parse_invalidContactCriteria_rejectsEmptyDuplicateAndMixedInput() {
        String invalid = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        for (String input : List.of("name/", "guardian/", "name/alex sch/", "guardian/janet name/",
                "alex guardian/janet", "alex name/alex", "guardian/janet day/Funday")) {
            assertParseFailure(parser, input, invalid);
        }
        assertParseFailure(parser, "name/alex name/bob",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, "guardian/janet\tguardian/john",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_GUARDIAN));
    }

    @Test
    public void parse_schoolOnly_matchesSubstringWithoutRequiringLessons() throws Exception {
        Model model = new ModelManager();
        Student raffles = new PersonBuilder().withName("Alex Tan")
                .withSchool(new School("Raffles Institution")).build();
        Student other = new PersonBuilder().withName("Bob Tan").withSchool(new School("Dunman High")).build();
        Student noSchool = new PersonBuilder().withName("Chris Tan").build();
        model.addPerson(raffles);
        model.addPerson(other);
        model.addPerson(noSchool);

        assertParseSuccess(parser, " sch/  RAFFLES  ",
                new FindCommand(search(Optional.of("raffles"), Optional.empty(), Optional.empty())));
        parser.parse("sch/fFlEs Ins").execute(model);
        assertEquals(List.of(raffles), model.getFilteredPersonList());
        parser.parse("sch/missing").execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void parse_schoolAndLevel_requiresBothRegardlessOfOrder() {
        FindCommand expected = new FindCommand(search(Optional.of("raffles"), Optional.of("secondary"),
                Optional.empty()));
        assertParseSuccess(parser, "sch/Raffles level/Secondary", expected);
        assertParseSuccess(parser, "level/SECONDARY\tsch/RAFFLES", expected);
    }

    @Test
    public void parse_schoolAndLessons_filtersSameLessonAndCalendarAndClearsSearch() throws Exception {
        Model model = new ModelManager();
        Lesson tuesdayMath = lesson("Math", DayOfWeek.TUESDAY);
        Lesson mondayEnglish = lesson("English", DayOfWeek.MONDAY);
        Lesson mondayMath = lesson("Math", DayOfWeek.MONDAY);
        Student alex = new PersonBuilder().withName("Alex Tan").withSchool(new School("Raffles Institution"))
                .withSchoolingLevel(new SchoolingLevel("Secondary 3"))
                .withLessons(tuesdayMath, mondayEnglish).build();
        Student other = new PersonBuilder().withName("Bob Tan").withSchool(new School("Dunman High"))
                .withSchoolingLevel(new SchoolingLevel("Secondary 3")).withLessons(mondayMath).build();
        model.addPerson(alex);
        model.addPerson(other);

        parser.parse("sch/raffles level/secondary subject/math day/mon").execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());

        Student matching = new PersonBuilder(alex).withLessons(tuesdayMath, mondayEnglish, mondayMath).build();
        model.setPerson(alex, matching);
        parser.parse("day/mon\nsch/RAFFLES subject/math level/secondary").execute(model);
        assertEquals(List.of(matching), model.getFilteredPersonList());
        new WeekCommand().execute(model);
        assertEquals(List.of(matching), model.getFilteredPersonList());
        assertFalse(model.getLessonPredicate().getValue().test(tuesdayMath));
        assertFalse(model.getLessonPredicate().getValue().test(mondayEnglish));
        assertTrue(model.getLessonPredicate().getValue().test(mondayMath));

        parser.parse("sch/raffles level/primary").execute(model);
        assertTrue(model.getFilteredPersonList().isEmpty());
        parser.parse("sch/raffles").execute(model);
        assertEquals(List.of(matching), model.getFilteredPersonList());
        assertTrue(model.getLessonPredicate().getValue().test(tuesdayMath));

        new ListCommand().execute(model);
        assertEquals(2, model.getFilteredPersonList().size());
    }

    @Test
    public void parse_editSchoolAfterSearch_clearsStudentAndLessonFilters() throws Exception {
        Lesson mondayMath = lesson("Math", DayOfWeek.MONDAY);
        Lesson tuesdayEnglish = lesson("English", DayOfWeek.TUESDAY);
        Student alex = new PersonBuilder().withName("Alex Tan").withSchool(new School("Raffles Institution"))
                .withLessons(mondayMath).build();
        Student other = new PersonBuilder().withName("Bob Tan").withSchool(new School("Dunman High"))
                .withLessons(tuesdayEnglish).build();
        AddressBookParser commandParser = new AddressBookParser();

        for (String schoolText : List.of("Dunman High", "")) {
            Model model = new ModelManager();
            model.addPerson(alex);
            model.addPerson(other);
            commandParser.parseCommand("find sch/raffles subject/math day/mon").execute(model);
            assertEquals(List.of(alex), model.getFilteredPersonList());
            assertFalse(model.getLessonPredicate().getValue().test(tuesdayEnglish));

            commandParser.parseCommand("edit 1 sch/" + schoolText).execute(model);

            Student edited = new PersonBuilder(alex)
                    .withSchool(schoolText.isEmpty() ? null : new School(schoolText)).build();
            assertEquals(List.of(edited, other), model.getFilteredPersonList());
            assertTrue(model.getLessonPredicate().getValue().test(tuesdayEnglish));
            commandParser.parseCommand("find sch/raffles").execute(model);
            assertTrue(model.getFilteredPersonList().isEmpty());
        }
    }

    @Test
    public void parse_invalidSchoolCriteria_throwsParseException() {
        String invalid = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        for (String input : List.of("sch/", "sch/   ", "sch/raffles level/", "sch/ day/mon",
                "alex sch/raffles", "sch/raffles day/Funday")) {
            assertParseFailure(parser, input, invalid);
        }
        assertParseFailure(parser, "sch/raffles\tsch/dunman",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SCHOOL));
        assertParseSuccess(parser, "presch/raffles", new FindCommand(
                new NameContainsKeywordsPredicate(List.of("presch/raffles"))));
    }

    private static Lesson lesson(String subject, DayOfWeek day) {
        return new Lesson(new Subject(subject),
                new WeeklyLessonSlot(day, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                new HourlyRate(new BigDecimal("45")));
    }

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
        FindCommand expected = new FindCommand(search(Optional.empty(), Optional.of("Primary 5"), Optional.empty()));
        assertParseSuccess(parser, " level/Primary 5", expected);
        assertParseSuccess(parser, "   level/  Primary   5  ", new FindCommand(
                search(Optional.empty(), Optional.of("Primary   5"), Optional.empty())));
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
                new FindCommand(search(Optional.empty(), Optional.empty(), Optional.of(wednesdayMath)), wednesdayMath));
        assertParseSuccess(parser, " day/Wednesday subject/Math level/Secondary",
                new FindCommand(search(Optional.empty(), Optional.of("Secondary"), Optional.of(wednesdayMath)),
                        wednesdayMath));

        LessonMatchesPredicate monday = new LessonMatchesPredicate(Optional.empty(), Optional.of(DayOfWeek.MONDAY));
        assertParseSuccess(parser, " day/mon",
                new FindCommand(search(Optional.empty(), Optional.empty(), Optional.of(monday)), monday));
    }

    private static StudentSearchPredicate search(Optional<String> school, Optional<String> level,
            Optional<LessonMatchesPredicate> lesson) {
        return new StudentSearchPredicate(Optional.empty(), Optional.empty(), school, level, lesson);
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
