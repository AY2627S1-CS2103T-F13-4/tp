package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.GuardianAddCommand;
import seedu.address.logic.commands.GuardianEditCommand;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.commands.LessonEditCommand;
import seedu.address.logic.commands.NoteAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Checks which error the user sees when an input contains several kinds of mistakes. */
public class CommandValidationOrderTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parse_invalidIndex_precedesPrefixErrors() {
        assertFormatError("edit 0 name/Alice name/Bob", EditCommand.MESSAGE_USAGE);
        assertFormatError("guardian-add 0 name/Alice name/Bob phone/123 bad/value",
                GuardianAddCommand.MESSAGE_USAGE);
        assertFormatError("guardian-edit 0 name/Alice name/Bob bad/value", GuardianEditCommand.MESSAGE_USAGE);
        assertFormatError("lesson-add 0 subject/Math subject/Science bad/value", LessonAddCommand.MESSAGE_USAGE);
        assertFormatError("lesson-edit 1 0 rate/10 rate/20 bad/value", LessonEditCommand.MESSAGE_USAGE);
        assertFormatError("note-add 0 date/2026-01-01 date/2026-02-01 text/Note", NoteAddCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_missingRequiredFields_precedesDuplicatePrefixes() {
        assertFormatError("guardian-add 1 name/Alice name/Bob bad/value", GuardianAddCommand.MESSAGE_USAGE);
        assertFormatError("lesson-add 1 subject/Math subject/Science bad/value", LessonAddCommand.MESSAGE_USAGE);
        assertFormatError("note-add 1 date/2026-01-01 date/2026-02-01", NoteAddCommand.MESSAGE_USAGE);
        assertFormatError("find name/Alice name/", FindCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_duplicatePrefixes_precedesUnknownPrefixes() {
        assertError("guardian-add 1 name/Alice name/Bob phone/123 bad/value",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertError("guardian-edit 1 name/Alice name/Bob bad/value",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertError("lesson-add 1 subject/Math subject/Science slot/Mon 1.00pm 2.00pm rate/45 bad/value",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SUBJECT));
        assertError("lesson-edit 1 1 rate/10 rate/20 bad/value",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_RATE));
    }

    private void assertFormatError(String input, String usage) {
        assertError(input, String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
    }

    private void assertError(String input, String expected) {
        ParseException exception = assertThrows(ParseException.class, () -> parser.parseCommand(input));
        assertEquals(expected, exception.getMessage(), input);
    }
}
