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
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.LessonCommand;
import seedu.address.logic.commands.LessonCommand.Operation;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;

public class LessonCommandParserTest {
    private static final String ADD_USAGE =
            "lesson-add STUDENT_INDEX subject/SUBJECT slot/LESSON_SLOT rate/HOURLY_RATE "
            + "[mode/MODE] [location/LOCATION]";
    private static final String EDIT_USAGE = "lesson-edit STUDENT_INDEX LESSON_INDEX [subject/SUBJECT] "
            + "[slot/LESSON_SLOT] [rate/HOURLY_RATE] [mode/MODE] [location/LOCATION] (at least one field required)";
    private static final String DELETE_USAGE = "lesson-delete STUDENT_INDEX LESSON_INDEX";

    private final LessonCommandParser addParser = new LessonCommandParser(Operation.ADD);
    private final LessonCommandParser editParser = new LessonCommandParser(Operation.EDIT);
    private final LessonCommandParser deleteParser = new LessonCommandParser(Operation.DELETE);

    private static String invalidFormat(String usage) {
        return String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage);
    }

    @Test
    public void parseAdd_allFields_success() {
        LessonCommand expected = new LessonCommand(INDEX_FIRST_PERSON, Operation.ADD, Optional.empty(),
                Optional.of(new Subject("Math")), Optional.of(VALID_LESSON_SLOT),
                Optional.of(new HourlyRate(new BigDecimal("45.50"))), Optional.empty(), Optional.empty());
        assertParseSuccess(addParser, "1 subject/  Math " + LESSON_SLOT_DESC.trim() + " rate/45.5", expected);
        assertParseSuccess(addParser, " 1 rate/45.50 subject/Math" + LESSON_SLOT_DESC, expected);
    }

    @Test
    public void parseAdd_optionalModeAndLocation_success() {
        LessonCommand expected = new LessonCommand(INDEX_FIRST_PERSON, Operation.ADD, Optional.empty(),
                Optional.of(new Subject("Math")), Optional.of(VALID_LESSON_SLOT),
                Optional.of(new HourlyRate(new BigDecimal("45"))),
                Optional.of(new LessonLocation("Blk 123 Clementi Ave 3")), Optional.of(LessonMode.PHYSICAL));
        assertParseSuccess(addParser, "1 subject/Math" + LESSON_SLOT_DESC + " rate/45 mode/PhysIcal "
                + "location/ Blk 123  Clementi Ave 3", expected);
        assertParseSuccess(addParser, "1 location/Blk 123 Clementi Ave 3 mode/physical subject/Math"
                + LESSON_SLOT_DESC + " rate/45", expected);
        assertParseSuccess(addParser, "1 subject/Math" + LESSON_SLOT_DESC
                + " rate/45 location/https://zoom.us/j/123 mode/virtual",
                new LessonCommand(INDEX_FIRST_PERSON, Operation.ADD, Optional.empty(),
                        Optional.of(new Subject("Math")), Optional.of(VALID_LESSON_SLOT),
                        Optional.of(new HourlyRate(new BigDecimal("45"))),
                        Optional.of(new LessonLocation("https://zoom.us/j/123")), Optional.of(LessonMode.VIRTUAL)));
    }

    @Test
    public void parseAddAndEdit_invalidModeOrLocation_failure() {
        String base = "1 subject/Math" + LESSON_SLOT_DESC + " rate/45";
        assertParseFailure(addParser, base + " mode/hybrid", LessonMode.MESSAGE_CONSTRAINTS);
        assertParseFailure(addParser, base + " mode/", LessonMode.MESSAGE_CONSTRAINTS);
        assertParseFailure(addParser, base + " location/", LessonLocation.MESSAGE_CONSTRAINTS);
        assertParseFailure(editParser, "1 2 mode/hybrid", LessonMode.MESSAGE_CONSTRAINTS);
        assertParseFailure(addParser, base + " location/" + "a".repeat(101), LessonLocation.MESSAGE_CONSTRAINTS);
        assertParseFailure(addParser, base + " location/home mode/virtual mode/physical",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_MODE));
        assertParseFailure(editParser, "1 2 location/a location/b",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LOCATION));
        assertParseFailure(deleteParser, "1 2 mode/virtual", invalidFormat(DELETE_USAGE));
    }

    @Test
    public void parseEdit_emptyModeOrLocation_clears() {
        assertParseSuccess(editParser, "1 2 mode/", new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.of(INDEX_SECOND_PERSON), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), false, true));
        assertParseSuccess(editParser, "1 2 location/  mode/virtual", new LessonCommand(INDEX_FIRST_PERSON,
                Operation.EDIT, Optional.of(INDEX_SECOND_PERSON), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.of(LessonMode.VIRTUAL), true, false));
    }

    @Test
    public void parseEdit_onlyModeOrLocation_success() {
        assertParseSuccess(editParser, "1 2 mode/virtual", new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.of(INDEX_SECOND_PERSON), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(LessonMode.VIRTUAL)));
        assertParseSuccess(editParser, "1 2 location/Library", new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.of(INDEX_SECOND_PERSON), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(new LessonLocation("Library")), Optional.empty()));
    }

    @Test
    public void parseAdd_invalid_failure() {
        assertParseFailure(addParser, "", invalidFormat(ADD_USAGE));
        assertParseFailure(addParser, "subject/Math" + LESSON_SLOT_DESC + " rate/45", invalidFormat(ADD_USAGE));
        assertParseFailure(addParser, "0 subject/Math" + LESSON_SLOT_DESC + " rate/45", invalidFormat(ADD_USAGE));
        assertParseFailure(addParser, "1 subject/Math rate/45", invalidFormat(ADD_USAGE));
        assertParseFailure(addParser, "1 subject/Math" + LESSON_SLOT_DESC + " rate/45 x/y", invalidFormat(ADD_USAGE));
        assertParseFailure(addParser, "1 extra subject/Math" + LESSON_SLOT_DESC + " rate/45", invalidFormat(ADD_USAGE));
        assertParseFailure(addParser, "1 subject/123" + LESSON_SLOT_DESC + " rate/45", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(addParser, "1 subject/Math slot/nope rate/45", WeeklyLessonSlot.MESSAGE_INVALID_FORMAT);
        assertParseFailure(addParser, "1 subject/Math slot/ rate/45", WeeklyLessonSlot.MESSAGE_INVALID_FORMAT);
        assertParseFailure(addParser, "1 subject/Math slot/Wed 5.00pm 1.00pm rate/45",
                WeeklyLessonSlot.MESSAGE_END_NOT_AFTER_START);
        assertParseFailure(addParser, "1 subject/Math" + LESSON_SLOT_DESC + " rate/-1", HourlyRate.MESSAGE_CONSTRAINTS);
        assertParseFailure(addParser, "1 subject/Math" + LESSON_SLOT_DESC + " rate/45 rate/50",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_RATE));
    }

    @Test
    public void parseEdit_someFields_success() {
        assertParseSuccess(editParser, "1 2 rate/50", new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.of(INDEX_SECOND_PERSON), Optional.empty(), Optional.empty(),
                Optional.of(new HourlyRate(new BigDecimal("50"))), Optional.empty(), Optional.empty()));
        assertParseSuccess(editParser, "  1   2  subject/Science" + LESSON_SLOT_DESC,
                new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT, Optional.of(INDEX_SECOND_PERSON),
                        Optional.of(new Subject("Science")), Optional.of(VALID_LESSON_SLOT), Optional.empty(),
                        Optional.empty(), Optional.empty()));
    }

    @Test
    public void parseEdit_invalid_failure() {
        assertParseFailure(editParser, "1 2", invalidFormat(EDIT_USAGE));
        assertParseFailure(editParser, "1 rate/50", invalidFormat(EDIT_USAGE));
        assertParseFailure(editParser, "1 2 3 rate/50", invalidFormat(EDIT_USAGE));
        assertParseFailure(editParser, "1 a rate/50", invalidFormat(EDIT_USAGE));
        assertParseFailure(editParser, "1 0 rate/50", invalidFormat(EDIT_USAGE));
        assertParseFailure(editParser, "1 2 subject/", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(editParser, "1 2 slot/Wednesday 1.00pm 5.00pm", WeeklyLessonSlot.MESSAGE_INVALID_FORMAT);
        assertParseFailure(editParser, "1 2 slot/Wed 1.00pm 1.00pm", WeeklyLessonSlot.MESSAGE_END_NOT_AFTER_START);
        assertParseFailure(editParser, "1 2 rate/1e2", HourlyRate.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parseDelete_success() {
        assertParseSuccess(deleteParser, "1 2", new LessonCommand(INDEX_FIRST_PERSON, Operation.DELETE,
                Optional.of(INDEX_SECOND_PERSON), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty()));
    }

    @Test
    public void parseDelete_invalid_failure() {
        assertParseFailure(deleteParser, "", invalidFormat(DELETE_USAGE));
        assertParseFailure(deleteParser, "1", invalidFormat(DELETE_USAGE));
        assertParseFailure(deleteParser, "1 2 3", invalidFormat(DELETE_USAGE));
        assertParseFailure(deleteParser, "1 2 rate/50", invalidFormat(DELETE_USAGE));
        assertParseFailure(deleteParser, "-1 2", invalidFormat(DELETE_USAGE));
    }
}
