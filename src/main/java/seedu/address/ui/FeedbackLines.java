package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits command feedback into lines that {@code ResultDisplay} styles differently:
 * a title, plain text, command formats and examples.
 * The usage messages it reads are written by each command, so it accepts each shape they use.
 */
final class FeedbackLines {

    /** How a line of feedback is shown. */
    enum Kind { TITLE, TEXT, FORMAT, EXAMPLE }

    /** One line of feedback and how to show it. */
    record Line(Kind kind, String text) {}

    private static final String EXAMPLE = "Example: ";
    private static final String PARAMETERS = "Parameters: ";
    /** "add: Adds a person. Parameters: name/NAME ..." or "find level/TEXT: Finds ...". */
    private static final Pattern COMMAND_DESCRIPTION = Pattern.compile("^([a-z]+(?:-[a-z]+)*(?: \\S+)?): (.+)$");
    /** A bare format line, e.g. "lesson-delete STUDENT_INDEX LESSON_INDEX". */
    private static final Pattern BARE_FORMAT = Pattern.compile("^[a-z]+(?:-[a-z]+)* [A-Z\\[].*$");

    private FeedbackLines() {}

    /**
     * Returns the lines of {@code message}. The first line is the title.
     */
    static List<Line> split(String message) {
        List<Line> lines = new ArrayList<>();
        String[] rawLines = message.strip().split("\n");
        lines.add(new Line(Kind.TITLE, rawLines[0].strip()));
        String commandWord = "";
        for (int i = 1; i < rawLines.length; i++) {
            String raw = rawLines[i].strip();
            if (raw.isEmpty()) {
                continue;
            }
            if (raw.startsWith(EXAMPLE)) {
                lines.add(new Line(Kind.EXAMPLE, raw.substring(EXAMPLE.length())));
                continue;
            }
            if (raw.startsWith(PARAMETERS)) {
                lines.add(new Line(Kind.FORMAT, (commandWord + " " + raw.substring(PARAMETERS.length())).strip()));
                continue;
            }
            Matcher described = COMMAND_DESCRIPTION.matcher(raw);
            if (described.matches()) {
                String command = described.group(1);
                String description = described.group(2);
                commandWord = command.split(" ")[0];
                int parameters = description.indexOf(PARAMETERS);
                if (parameters >= 0) {
                    lines.add(new Line(Kind.TEXT, description.substring(0, parameters).strip()));
                    lines.add(new Line(Kind.FORMAT,
                            command + " " + description.substring(parameters + PARAMETERS.length()).strip()));
                } else {
                    lines.add(new Line(Kind.TEXT, description));
                    if (!command.equals(commandWord)) {
                        lines.add(new Line(Kind.FORMAT, command));
                    }
                }
                continue;
            }
            if (BARE_FORMAT.matcher(raw).matches()) {
                lines.add(new Line(Kind.FORMAT, raw));
                continue;
            }
            lines.add(new Line(Kind.TEXT, raw));
        }
        return lines;
    }
}
