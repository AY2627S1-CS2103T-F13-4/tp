package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Comparator;
import java.util.Objects;

/** An immutable, dated note on a student's progress, such as what was covered in a lesson. */
public final class ProgressNote {
    public static final int MAX_TEXT_LENGTH = 500;
    public static final String MESSAGE_DATE_CONSTRAINTS =
            "Note dates must be real dates in YYYY-MM-DD format, such as 2026-10-15.";
    public static final String MESSAGE_TEXT_CONSTRAINTS =
            "Note text must contain 1-" + MAX_TEXT_LENGTH + " characters and cannot be blank.";
    /** Orders notes by date, oldest first. Notes on the same date keep their order. */
    public static final Comparator<ProgressNote> OLDEST_FIRST = Comparator.comparing(ProgressNote::getDate);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final LocalDate date;
    private final String text;

    /** Creates a note, trimming spaces around {@code text}. */
    public ProgressNote(LocalDate date, String text) {
        requireNonNull(date);
        requireNonNull(text);
        checkArgument(isValidText(text), MESSAGE_TEXT_CONSTRAINTS);
        this.date = date;
        this.text = text.strip();
    }

    /**
     * Returns whether {@code text}, once trimmed, is 1 to 500 characters on a single line.
     * Characters are counted as code points, so an emoji counts as one.
     */
    public static boolean isValidText(String text) {
        requireNonNull(text);
        String trimmed = text.strip();
        int length = trimmed.codePointCount(0, trimmed.length());
        // Control characters and Unicode line or paragraph separators would split the note over lines.
        return length > 0 && length <= MAX_TEXT_LENGTH && !trimmed.matches("(?s).*[\\p{Cc}\\p{Zl}\\p{Zp}].*");
    }

    /**
     * Parses a date in YYYY-MM-DD format.
     *
     * @throws IllegalArgumentException if {@code value} is not a real date in that format.
     */
    public static LocalDate parseDate(String value) {
        requireNonNull(value);
        try {
            return LocalDate.parse(value.strip(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(MESSAGE_DATE_CONSTRAINTS, e);
        }
    }

    /** Returns {@code date} in YYYY-MM-DD format, as typed and stored. */
    public static String formatDate(LocalDate date) {
        return DATE_FORMAT.format(date);
    }

    public LocalDate getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ProgressNote note && date.equals(note.date) && text.equals(note.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, text);
    }

    @Override
    public String toString() {
        return formatDate(date) + " " + text;
    }
}
