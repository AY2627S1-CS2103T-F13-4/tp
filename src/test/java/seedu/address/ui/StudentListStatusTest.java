package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class StudentListStatusTest {

    @Test
    public void summary_countsStudents() {
        assertEquals("", StudentListStatus.summary(0, 0));
        assertEquals("1 student", StudentListStatus.summary(1, 1));
        assertEquals("8 students", StudentListStatus.summary(8, 8));
    }

    // After a find, the count must say the list is filtered, so the tutor knows others are hidden.
    @Test
    public void summary_filteredList_showsShownOfTotal() {
        assertEquals("Showing 1 of 8", StudentListStatus.summary(1, 8));
        assertEquals("Showing 0 of 8", StudentListStatus.summary(0, 8));
    }

    // An empty book and a search with no matches need different next steps: add a student, or list all.
    @Test
    public void emptyState_distinguishesEmptyBookFromNoMatches() {
        assertNotEquals(StudentListStatus.emptyTitle(0), StudentListStatus.emptyTitle(3));
        assertEquals("Add one with: add n/NAME p/PHONE e/EMAIL a/ADDRESS", StudentListStatus.emptyDetail(0));
        assertEquals("Type list to show all 3 students.", StudentListStatus.emptyDetail(3));
        assertTrue(StudentListStatus.emptyDetail(1).endsWith("1 student."));
    }
}
