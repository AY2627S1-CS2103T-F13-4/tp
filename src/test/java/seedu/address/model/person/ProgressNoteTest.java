//@@author pranavp311
package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class ProgressNoteTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 15);

    @Test
    public void constructor_trimsText() {
        assertEquals("Finished fractions", new ProgressNote(DATE, "  Finished fractions ").getText());
    }

    @Test
    public void constructor_invalidText_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ProgressNote(DATE, "   "));
        assertThrows(IllegalArgumentException.class, () -> new ProgressNote(DATE, "a".repeat(501)));
        assertThrows(NullPointerException.class, () -> new ProgressNote(null, "text"));
    }

    @Test
    public void isValidText() {
        assertTrue(ProgressNote.isValidText("a"));
        assertTrue(ProgressNote.isValidText("a".repeat(500)));
        // the limit applies after trimming
        assertTrue(ProgressNote.isValidText(" " + "a".repeat(500) + " "));
        // a word followed by '/' is ordinary text in a note
        assertTrue(ProgressNote.isValidText("Revise and/or redo Q3 w/o a calculator"));

        assertFalse(ProgressNote.isValidText(""));
        assertFalse(ProgressNote.isValidText(" \t "));
        assertFalse(ProgressNote.isValidText("a".repeat(501)));
        // an emoji counts as one character
        assertTrue(ProgressNote.isValidText("\uD83D\uDE00".repeat(500)));
        assertFalse(ProgressNote.isValidText("\uD83D\uDE00".repeat(501)));
        // a note is one line, as typed in the command box
        for (String lineBreak : new String[] {"\n", "\r", "\u0085", "\u2028", "\u2029"}) {
            assertFalse(ProgressNote.isValidText("line one" + lineBreak + "line two"), lineBreak);
        }
    }

    @Test
    public void parseDate() {
        assertEquals(DATE, ProgressNote.parseDate("2026-10-15"));
        assertEquals(LocalDate.of(2024, 2, 29), ProgressNote.parseDate(" 2024-02-29 "));

        // not a real date, or not YYYY-MM-DD
        for (String invalid : new String[] {"2026-02-29", "2026-13-01", "2026-10-5", "15-10-2026", "2026/10/15",
            "today", ""}) {
            assertThrows(IllegalArgumentException.class, () -> ProgressNote.parseDate(invalid), invalid);
        }
    }

    @Test
    public void formatDate_roundTrips() {
        assertEquals("2026-01-05", ProgressNote.formatDate(LocalDate.of(2026, 1, 5)));
        assertEquals(DATE, ProgressNote.parseDate(ProgressNote.formatDate(DATE)));
    }

    @Test
    public void equals() {
        ProgressNote note = new ProgressNote(DATE, "Finished fractions");
        assertEquals(note, new ProgressNote(DATE, " Finished fractions"));
        assertEquals(note.hashCode(), new ProgressNote(DATE, "Finished fractions").hashCode());
        assertNotEquals(note, new ProgressNote(DATE.plusDays(1), "Finished fractions"));
        assertNotEquals(note, new ProgressNote(DATE, "finished fractions"));
        assertNotEquals(note, null);
    }
}
