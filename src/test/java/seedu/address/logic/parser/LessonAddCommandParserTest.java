package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.LESSON_SLOT_DESC;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LESSON_SLOT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;

public class LessonAddCommandParserTest {
    private static final String USAGE = LessonAddCommand.MESSAGE_USAGE;

    private final LessonAddCommandParser parser = new LessonAddCommandParser();

    private static String invalidFormat(String usage) {
        return String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage);
    }

    @Test
    public void parseAdd_allFields_success() {
        LessonAddCommand expected = new LessonAddCommand(INDEX_FIRST_PERSON,
                new Lesson(new Subject("Math"), VALID_LESSON_SLOT, new HourlyRate(new BigDecimal("45.50")),
                        Optional.empty(), Optional.empty()));
        assertParseSuccess(parser, "1 subject/  Math " + LESSON_SLOT_DESC.trim() + " rate/45.5", expected);
        assertParseSuccess(parser, " 1 rate/45.50 subject/Math" + LESSON_SLOT_DESC, expected);
    }

    @Test
    public void parseAdd_optionalModeAndLocation_success() {
        LessonAddCommand expected = new LessonAddCommand(INDEX_FIRST_PERSON,
                new Lesson(new Subject("Math"), VALID_LESSON_SLOT, new HourlyRate(new BigDecimal("45")),
                        Optional.of(new LessonLocation("Blk 123 Clementi Ave 3")), Optional.of(LessonMode.PHYSICAL)));
        assertParseSuccess(parser, "1 subject/Math" + LESSON_SLOT_DESC + " rate/45 mode/PhysIcal "
                + "location/ Blk 123  Clementi Ave 3", expected);
        assertParseSuccess(parser, "1 location/Blk 123 Clementi Ave 3 mode/physical subject/Math"
                + LESSON_SLOT_DESC + " rate/45", expected);
        assertParseSuccess(parser, "1 subject/Math" + LESSON_SLOT_DESC
                + " rate/45 location/https://zoom.us/j/123 mode/virtual",
                new LessonAddCommand(INDEX_FIRST_PERSON,
                        new Lesson(new Subject("Math"), VALID_LESSON_SLOT, new HourlyRate(new BigDecimal("45")),
                                Optional.of(new LessonLocation("https://zoom.us/j/123")),
                                Optional.of(LessonMode.VIRTUAL))));
    }

    @Test
    public void parseAdd_invalid_failure() {
        assertParseFailure(parser, "", invalidFormat(USAGE));
        assertParseFailure(parser, "subject/Math" + LESSON_SLOT_DESC + " rate/45", invalidFormat(USAGE));
        assertParseFailure(parser, "0 subject/Math" + LESSON_SLOT_DESC + " rate/45", invalidFormat(USAGE));
        assertParseFailure(parser, "1 subject/Math rate/45", invalidFormat(USAGE));
        assertParseFailure(parser, "1 subject/Math" + LESSON_SLOT_DESC + " rate/45 x/y", invalidFormat(USAGE));
        assertParseFailure(parser, "1 extra subject/Math" + LESSON_SLOT_DESC + " rate/45", invalidFormat(USAGE));
        assertParseFailure(parser, "1 subject/123" + LESSON_SLOT_DESC + " rate/45", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 subject/Math slot/nope rate/45", WeeklyLessonSlot.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 subject/Math" + LESSON_SLOT_DESC + " rate/-1", HourlyRate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 subject/Math" + LESSON_SLOT_DESC + " rate/45 rate/50",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_RATE));
    }

    @Test
    public void parse_invalidModeOrLocation_failure() {
        String base = "1 subject/Math" + LESSON_SLOT_DESC + " rate/45";
        assertParseFailure(parser, base + " mode/hybrid", LessonMode.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, base + " mode/", LessonMode.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, base + " location/", LessonLocation.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, base + " location/" + "a".repeat(101), LessonLocation.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, base + " location/home mode/virtual mode/physical",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_MODE));
        assertParseFailure(parser, base + " location/a location/b",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LOCATION));
    }
}
