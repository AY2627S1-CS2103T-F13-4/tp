package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class ClashWorkflowTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void warnings_coverIdenticalContainingAndMultipleIntervalsAndPersist() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Alice").build());
        model.addPerson(new PersonBuilder().withName("Bob").build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("students.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        logic.execute("lesson-add 1 subject/Math slot/Mon 2.00pm 3.00pm rate/40");
        assertFalse(logic.execute("lesson-edit 1 1 rate/45").getFeedbackToUser().contains("Warning"));
        String result = logic.execute("lesson-add 2 subject/English slot/Mon 2.00pm 3.00pm rate/50")
                .getFeedbackToUser();
        assertTrue(result.contains("\n- Alice, lesson 1"));
        assertFalse(result.contains("\n- Bob"));
        result = logic.execute("lesson-edit 2 1 slot/Mon 1.00pm 4.00pm").getFeedbackToUser();
        assertTrue(result.contains("\n- Alice, lesson 1"));
        result = logic.execute("lesson-add 2 subject/Science slot/Mon 2.15pm 2.45pm rate/60")
                .getFeedbackToUser();
        assertTrue(result.contains("overlaps with 2 lesson(s):"));
        assertTrue(result.contains("\n- Alice, lesson 1"));
        assertTrue(result.contains("\n- Bob, lesson 1"));
        assertFalse(result.contains("\n- Bob, lesson 2"));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());

        result = logic.execute("lesson-edit 2 2 rate/70").getFeedbackToUser();
        assertTrue(result.contains("overlaps with 2 lesson(s):"));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        logic.execute("lesson-edit 2 2 slot/Tue 2.15pm 2.45pm");
        assertFalse(logic.execute("lesson-edit 2 2 rate/75").getFeedbackToUser().contains("Warning"));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void clashes_includeHiddenStudentsAndAllowChanges() throws Exception {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Alice").build());
        model.addPerson(new PersonBuilder().withName("Bob").build());
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("lesson-add 1 subject/Math slot/Mon 2.00pm 3.00pm rate/40").execute(model);
        parser.parseCommand("find Bob").execute(model);
        String result = parser.parseCommand("lesson-add 1 subject/English slot/Mon 2.30pm 4.00pm rate/50")
                .execute(model).getFeedbackToUser();
        assertTrue(result.contains("Alice, lesson 1"));
        assertTrue(model.getFilteredPersonList().get(0).getLessons().size() == 1);
        result = parser.parseCommand("lesson-edit 1 1 slot/Mon 3.00pm 4.00pm")
                .execute(model).getFeedbackToUser();
        assertFalse(result.contains("Warning"));
        result = parser.parseCommand("lesson-edit 1 1 slot/Tue 2.00pm 3.00pm")
                .execute(model).getFeedbackToUser();
        assertFalse(result.contains("Warning"));
        result = parser.parseCommand("lesson-add 1 subject/Science slot/Tue 2.15pm 2.45pm rate/60")
                .execute(model).getFeedbackToUser();
        assertTrue(result.contains("Bob, lesson 1"));
        result = parser.parseCommand("lesson-delete 1 2").execute(model).getFeedbackToUser();
        assertFalse(result.contains("Warning"));
    }
}
