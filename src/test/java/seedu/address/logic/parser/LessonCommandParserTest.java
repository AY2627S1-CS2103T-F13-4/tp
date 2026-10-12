package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.LESSON_SLOT_DESC;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LESSON_SLOT;
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
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;

public class LessonCommandParserTest {
    private static final String ADD_USAGE = "lesson-add STUDENT_INDEX s/SUBJECT i/LESSON_SLOT r/HOURLY_RATE";
    private static final String EDIT_USAGE = "lesson-edit STUDENT_INDEX LESSON_INDEX [s/SUBJECT] "
            + "[i/LESSON_SLOT] [r/HOURLY_RATE] (at least one field required)";
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
                Optional.of(new HourlyRate(new BigDecimal("45.50"))));
        assertParseSuccess(addParser, "1 s/  Math " + LESSON_SLOT_DESC.trim() + " r/45.5", expected);
        assertParseSuccess(addParser, " 1 r/45.50 s/Math" + LESSON_SLOT_DESC, expected);
    }

    @Test
    public void parseAdd_invalid_failure() {
        assertParseFailure(addParser, "", invalidFormat(ADD_USAGE));
        assertParseFailure(addParser, "s/Math" + LESSON_SLOT_DESC + " r/45", invalidFormat(ADD_USAGE));
        assertParseFailure(addParser, "0 s/Math" + LESSON_SLOT_DESC + " r/45", invalidFormat(ADD_USAGE));
        assertParseFailure(addParser, "1 s/Math r/45", invalidFormat(ADD_USAGE));
        assertParseFailure(addParser, "1 s/Math" + LESSON_SLOT_DESC + " r/45 x/y", invalidFormat(ADD_USAGE));
        assertParseFailure(addParser, "1 extra s/Math" + LESSON_SLOT_DESC + " r/45", invalidFormat(ADD_USAGE));
        assertParseFailure(addParser, "1 s/123" + LESSON_SLOT_DESC + " r/45", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(addParser, "1 s/Math i/nope r/45", WeeklyLessonSlot.MESSAGE_CONSTRAINTS);
        assertParseFailure(addParser, "1 s/Math" + LESSON_SLOT_DESC + " r/-1", HourlyRate.MESSAGE_CONSTRAINTS);
        assertParseFailure(addParser, "1 s/Math" + LESSON_SLOT_DESC + " r/45 r/50",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_RATE));
    }

    @Test
    public void parseEdit_someFields_success() {
        assertParseSuccess(editParser, "1 2 r/50", new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT,
                Optional.of(INDEX_SECOND_PERSON), Optional.empty(), Optional.empty(),
                Optional.of(new HourlyRate(new BigDecimal("50")))));
        assertParseSuccess(editParser, "  1   2  s/Science" + LESSON_SLOT_DESC,
                new LessonCommand(INDEX_FIRST_PERSON, Operation.EDIT, Optional.of(INDEX_SECOND_PERSON),
                        Optional.of(new Subject("Science")), Optional.of(VALID_LESSON_SLOT), Optional.empty()));
    }

    @Test
    public void parseEdit_invalid_failure() {
        assertParseFailure(editParser, "1 2", invalidFormat(EDIT_USAGE));
        assertParseFailure(editParser, "1 r/50", invalidFormat(EDIT_USAGE));
        assertParseFailure(editParser, "1 2 3 r/50", invalidFormat(EDIT_USAGE));
        assertParseFailure(editParser, "1 a r/50", invalidFormat(EDIT_USAGE));
        assertParseFailure(editParser, "1 0 r/50", invalidFormat(EDIT_USAGE));
        assertParseFailure(editParser, "1 2 s/", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(editParser, "1 2 r/1e2", HourlyRate.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parseDelete_success() {
        assertParseSuccess(deleteParser, "1 2", new LessonCommand(INDEX_FIRST_PERSON, Operation.DELETE,
                Optional.of(INDEX_SECOND_PERSON), Optional.empty(), Optional.empty(), Optional.empty()));
    }

    @Test
    public void parseDelete_invalid_failure() {
        assertParseFailure(deleteParser, "", invalidFormat(DELETE_USAGE));
        assertParseFailure(deleteParser, "1", invalidFormat(DELETE_USAGE));
        assertParseFailure(deleteParser, "1 2 3", invalidFormat(DELETE_USAGE));
        assertParseFailure(deleteParser, "1 2 r/50", invalidFormat(DELETE_USAGE));
        assertParseFailure(deleteParser, "-1 2", invalidFormat(DELETE_USAGE));
    }
}
