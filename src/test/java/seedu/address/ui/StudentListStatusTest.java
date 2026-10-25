package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;

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
        assertEquals("Add your first student, for example:", StudentListStatus.emptyDetail(0));
        assertEquals("Show all 3 students with:", StudentListStatus.emptyDetail(3));
        assertEquals("Show all 1 student with:", StudentListStatus.emptyDetail(1));
        assertEquals("list", StudentListStatus.emptyCommand(3));
    }

    @Test
    public void emptyState_archive_suggestsTheAppropriateStudentList() {
        assertEquals("No archived students yet", StudentListStatus.emptyTitle(0, true));
        assertEquals("View your active students with:", StudentListStatus.emptyDetail(0, true));
        assertEquals("list", StudentListStatus.emptyCommand(0, true));
        assertEquals("No students match", StudentListStatus.emptyTitle(3, true));
        assertEquals("Show all 3 students with:", StudentListStatus.emptyDetail(3, true));
        assertEquals("list-archive", StudentListStatus.emptyCommand(3, true));
    }

    // The empty states show commands to type as they are, so each must work in the state it is shown in:
    // the add on an empty book, then the week screen's lesson-add on the student it just added.
    @Test
    public void emptyCommands_runAsShown() throws Exception {
        Model model = new ModelManager();
        AddressBookParser parser = new AddressBookParser();

        parser.parseCommand(StudentListStatus.emptyCommand(0)).execute(model);
        assertEquals(1, model.getFilteredPersonList().size());

        parser.parseCommand(WeekPanel.EMPTY_COMMAND).execute(model);
        assertEquals(1, model.getFilteredPersonList().get(0).getLessons().size());
    }
}
