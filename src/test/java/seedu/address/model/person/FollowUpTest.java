package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class FollowUpTest {

    @Test
    public void constructor_trimsText() {
        assertEquals("Send report", new FollowUp("  Send report \t").getText());
    }

    @Test
    public void constructor_invalidText_throwsIllegalArgumentException() {
        assertThrows(NullPointerException.class, () -> new FollowUp(null));
        assertThrows(IllegalArgumentException.class, () -> new FollowUp("   "));
    }

    @Test
    public void isValidText() {
        assertTrue(FollowUp.isValidText("a"));
        assertTrue(FollowUp.isValidText("a".repeat(FollowUp.MAX_TEXT_LENGTH)));
        assertTrue(FollowUp.isValidText("Ask about P5 and/or P6 books"));
        // Code points are counted, so emoji at the limit still fit.
        assertTrue(FollowUp.isValidText("📚".repeat(FollowUp.MAX_TEXT_LENGTH)));

        assertFalse(FollowUp.isValidText(""));
        assertFalse(FollowUp.isValidText(" \t "));
        assertFalse(FollowUp.isValidText("a".repeat(FollowUp.MAX_TEXT_LENGTH + 1)));
        assertFalse(FollowUp.isValidText("Line one\nLine two"));
        assertFalse(FollowUp.isValidText("Line one Line two"));
    }

    @Test
    public void isSameFollowUp_ignoresCase() {
        FollowUp report = new FollowUp("Send report");
        assertTrue(report.isSameFollowUp(new FollowUp("SEND REPORT")));
        assertFalse(report.isSameFollowUp(new FollowUp("Send reports")));
        assertFalse(report.isSameFollowUp(null));
    }

    @Test
    public void equals() {
        FollowUp report = new FollowUp("Send report");
        assertEquals(report, report);
        assertEquals(report, new FollowUp(" Send report "));
        assertEquals(report.hashCode(), new FollowUp("Send report").hashCode());
        assertNotEquals(report, new FollowUp("send report"));
        assertNotEquals(report, null);
        assertNotEquals(report, "Send report");
        assertEquals("Send report", report.toString());
    }
}
