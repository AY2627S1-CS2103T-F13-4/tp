package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NoteAddCommand;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ProgressNote;

public class NoteWorkflowTest {
    private static final ProgressNote OCTOBER = new ProgressNote(LocalDate.of(2026, 10, 15), "Fractions");

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private static NoteAddCommand add(Index student, ProgressNote note) {
        return new NoteAddCommand(student, note);
    }

    @Test
    public void otherStudentCommands_keepNotes() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        add(INDEX_FIRST_PERSON, OCTOBER).execute(model);
        for (String command : List.of("edit 1 phone/98765432", "guardian-add 1 name/Janet Tan phone/91234567",
                "guardian-edit 1 phone/87654321", "lesson-add 1 subject/Math slot/Mon 4.00pm 5.00pm rate/40",
                "lesson-edit 1 1 rate/45", "lesson-delete 1 1", "guardian-delete 1")) {
            parser.parseCommand(command).execute(model);
            assertEquals(List.of(OCTOBER), model.getFilteredPersonList().get(0).getNotes(), command);
        }
    }
}
