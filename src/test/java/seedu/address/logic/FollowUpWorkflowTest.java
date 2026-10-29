package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.Student;
import seedu.address.testutil.PersonBuilder;

public class FollowUpWorkflowTest {
    private static final FollowUp REPORT = new FollowUp("Send the term 4 progress report");
    private static final FollowUp MAKE_UP = new FollowUp("Confirm the make-up lesson");

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    /** Gives the first displayed student the follow-ups REPORT then MAKE_UP, and returns that student. */
    private Student giveFirstStudentTwoItems() {
        Student first = model.getFilteredPersonList().get(0);
        Student withItems = new PersonBuilder(first)
                .withFollowUps(REPORT.getText(), MAKE_UP.getText()).build();
        model.setPerson(first, withItems);
        return withItems;
    }

    @Test
    public void otherStudentCommands_keepFollowUps() throws Exception {
        giveFirstStudentTwoItems();
        AddressBookParser parser = new AddressBookParser();
        for (String command : List.of("edit 1 phone/90001111", "guardian-add 1 name/Janet Tan phone/91234567",
                "guardian-delete 1", "lesson-add 1 subject/Math slot/Wed 1.00pm 5.00pm rate/45",
                "note-add 1 date/2020-01-15 text/Fractions")) {
            parser.parseCommand(command).execute(model);
            assertEquals(List.of(REPORT, MAKE_UP), model.getFilteredPersonList().get(0).getFollowUps(), command);
        }
    }

    @Test
    public void archiveAndUnarchive_keepFollowUpsAndNotes() throws Exception {
        Student original = giveFirstStudentTwoItems();
        Student withNote = new PersonBuilder(original)
                .withNotes(new ProgressNote(LocalDate.of(2020, 1, 15), "Fractions")).build();
        model.setPerson(original, withNote);

        Student restored = withNote.withArchived(true).withArchived(false);

        assertEquals(withNote, restored);
        assertEquals(List.of(REPORT, MAKE_UP), restored.getFollowUps());
    }
}
