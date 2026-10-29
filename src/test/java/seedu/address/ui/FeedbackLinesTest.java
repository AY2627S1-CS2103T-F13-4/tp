package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.ui.FeedbackLines.Kind;
import seedu.address.ui.FeedbackLines.Line;

public class FeedbackLinesTest {

    @Test
    public void singleLineMessage_isOnlyTitle() {
        assertEquals(List.of(new Line(Kind.TITLE, "Listed all students.")),
                FeedbackLines.split("Listed all students."));
    }

    // Usage with "Parameters:" inline: the format must include the command word so it can be typed as shown.
    @Test
    public void inlineParameters_becomeFullFormat() {
        String usage = "Invalid command format!\n"
                + "add: Adds a person. Parameters: name/NAME phone/PHONE\n"
                + "Example: add name/John Doe phone/98765432";
        assertEquals(List.of(new Line(Kind.TITLE, "Invalid command format!"),
                new Line(Kind.TEXT, "Adds a person."),
                new Line(Kind.FORMAT, "add name/NAME phone/PHONE"),
                new Line(Kind.EXAMPLE, "add name/John Doe phone/98765432")), FeedbackLines.split(usage));
    }

    @Test
    public void separateParametersLine_usesPrecedingCommandWord() {
        String usage = "Invalid command format!\n"
                + "delete: Deletes the person.\n"
                + "Parameters: INDEX\n"
                + "Example: delete 1";
        assertEquals(List.of(new Line(Kind.TITLE, "Invalid command format!"),
                new Line(Kind.TEXT, "Deletes the person."),
                new Line(Kind.FORMAT, "delete INDEX"),
                new Line(Kind.EXAMPLE, "delete 1")), FeedbackLines.split(usage));
    }

    @Test
    public void bareFormatAndCommandWithArgument_areFormats() {
        assertEquals(new Line(Kind.FORMAT, "lesson-delete STUDENT_INDEX LESSON_INDEX"),
                FeedbackLines.split("Invalid command format!\nlesson-delete STUDENT_INDEX LESSON_INDEX").get(1));
        assertEquals(List.of(new Line(Kind.TITLE, "Invalid command format!"),
                new Line(Kind.TEXT, "Finds by level."),
                new Line(Kind.FORMAT, "find level/TEXT")),
                FeedbackLines.split("Invalid command format!\nfind level/TEXT: Finds by level."));
    }

    // Sentences that merely mention a prefix stay plain text.
    @Test
    public void plainSentence_staysText() {
        assertEquals(new Line(Kind.TEXT, "Use an empty level/ to clear the schooling level."),
                FeedbackLines.split("Invalid command format!\n"
                        + "Use an empty level/ to clear the schooling level.").get(1));
    }

    // Every command's real usage error must yield a Format line, so a teammate rewording a usage message
    // into a shape this class does not recognise makes this test fail instead of silently losing the format.
    @Test
    public void realUsageErrors_allShowAFormat() {
        String[] incompleteCommands = {"add", "edit", "delete", "find", "lesson-add", "lesson-edit", "lesson-delete",
            "guardian-add", "guardian-edit", "guardian-delete"};
        for (String command : incompleteCommands) {
            List<Line> lines = FeedbackLines.split(usageError(command));
            assertTrue(lines.stream().anyMatch(line -> line.kind() == Kind.FORMAT
                    && line.text().startsWith(command)), command + " has no format line: " + lines);
        }
    }

    // Commands whose usage includes an example must show it as an Example line.
    @Test
    public void realUsageErrors_withExamples_showExample() {
        for (String command : new String[] {"add", "edit", "delete", "find"}) {
            List<Line> lines = FeedbackLines.split(usageError(command));
            assertTrue(lines.stream().anyMatch(line -> line.kind() == Kind.EXAMPLE
                    && line.text().startsWith(command)), command + " has no example line: " + lines);
        }
    }

    private static String usageError(String command) {
        try {
            new AddressBookParser().parseCommand(command);
        } catch (ParseException e) {
            return e.getMessage();
        }
        return fail(command + " parsed without arguments");
    }
}
