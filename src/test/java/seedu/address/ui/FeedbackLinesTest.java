package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.ui.FeedbackLines.Kind;
import seedu.address.ui.FeedbackLines.Line;

public class FeedbackLinesTest {

    @Test
    public void singleLineMessage_isOnlyTitle() {
        assertEquals(List.of(new Line(Kind.TITLE, "Listed all persons.")), FeedbackLines.split("Listed all persons."));
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
                + "Parameters: INDEX (must be a positive integer)\n"
                + "Example: delete 1";
        assertEquals(List.of(new Line(Kind.TITLE, "Invalid command format!"),
                new Line(Kind.TEXT, "Deletes the person."),
                new Line(Kind.FORMAT, "delete INDEX (must be a positive integer)"),
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
}
