package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.LESSON_SLOT_DESC;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LESSON_SLOT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.LessonEditCommand;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.Subject;
import seedu.address.testutil.EditLessonDescriptorBuilder;

public class LessonEditCommandParserTest {
    private static final String USAGE = LessonEditCommand.MESSAGE_USAGE;

    private final LessonEditCommandParser parser = new LessonEditCommandParser();

    private static String invalidFormat(String usage) {
        return String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage);
    }

    @Test
    public void parseEdit_emptyModeOrLocation_clears() {
        assertParseSuccess(parser, "1 2 mode/", new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                new EditLessonDescriptorBuilder()
                        .withLessonMode(Optional.empty()).build()));
        assertParseSuccess(parser, "1 2 location/  mode/virtual",
                new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                        new EditLessonDescriptorBuilder()
                                .withLessonLocation(Optional.empty())
                                .withLessonMode(Optional.of(LessonMode.VIRTUAL)).build()));
    }

    @Test
    public void parseEdit_onlyModeOrLocation_success() {
        assertParseSuccess(parser, "1 2 mode/virtual",
                new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                        new EditLessonDescriptorBuilder()
                                .withLessonMode(Optional.of(LessonMode.VIRTUAL)).build()));
        assertParseSuccess(parser, "1 2 location/Library",
                new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                        new EditLessonDescriptorBuilder()
                                .withLessonLocation(Optional.of(new LessonLocation("Library"))).build()));
    }

    @Test
    public void parseEdit_someFields_success() {
        assertParseSuccess(parser, "1 2 rate/50", new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                new EditLessonDescriptorBuilder()
                        .withHourlyRate(new HourlyRate(new BigDecimal("50"))).build()));
        assertParseSuccess(parser, "  1   2  subject/Science" + LESSON_SLOT_DESC,
                new LessonEditCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON,
                        new EditLessonDescriptorBuilder()
                                .withSubject(new Subject("Science"))
                                .withLessonSlot(VALID_LESSON_SLOT).build()));
    }

    @Test
    public void parseEdit_invalid_failure() {
        assertParseFailure(parser, "1 2", LessonEditCommand.MESSAGE_NOT_EDITED);
        assertParseFailure(parser, "1 rate/50", invalidFormat(USAGE));
        assertParseFailure(parser, "1 2 3 rate/50", invalidFormat(USAGE));
        assertParseFailure(parser, "1 a rate/50", invalidFormat(USAGE));
        assertParseFailure(parser, "1 0 rate/50", invalidFormat(USAGE));
        assertParseFailure(parser, "1 2 subject/", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 2 rate/1e2", HourlyRate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 2 subject/Math x/y", invalidFormat(USAGE));
        assertParseFailure(parser, "1 2 location/Library x/y", invalidFormat(USAGE));
        assertParseFailure(parser, "1 2 subject/Math subject/Science",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SUBJECT));
    }

    @Test
    public void parse_invalidModeOrLocation_failure() {
        assertParseFailure(parser, "1 2 mode/hybrid", LessonMode.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 2 location/a location/b",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LOCATION));
    }

    @Test
    public void parse_whitespaceBetweenIndexes_success() {
        assertParseSuccess(parser, " \t1\t 2\t rate/50", new LessonEditCommand(
                INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, new EditLessonDescriptorBuilder()
                        .withHourlyRate(new HourlyRate(new BigDecimal("50"))).build()));
    }
}
