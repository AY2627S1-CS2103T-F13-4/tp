package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Tests the hourly rate from command input through persistence. */
public class HourlyRateWorkflowTest {
    private static final String ADD = "add n/Alex Tan p/91234567 e/alex@example.com a/Clementi";

    @TempDir
    public Path temporaryFolder;
    private ModelManager model;
    private LogicManager logic;
    private JsonAddressBookStorage storage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void rate_canBeAddedEditedClearedAndPreserved() throws Exception {
        assertTrue(logic.execute(ADD + " r/45.5").getFeedbackToUser().contains("Rate: SGD 45.50/hour"));
        assertEquals(Optional.of(new HourlyRate(new BigDecimal("45.50"))), reloaded().getHourlyRate());

        logic.execute("edit 1 p/90000000");
        assertEquals(Optional.of(new HourlyRate(new BigDecimal("45.50"))), reloaded().getHourlyRate());

        assertTrue(logic.execute("edit 1 r/50").getFeedbackToUser().contains("Rate: SGD 50.00/hour"));
        assertEquals(Optional.of(new HourlyRate(new BigDecimal("50"))), reloaded().getHourlyRate());

        logic.execute("edit 1 r/");
        assertTrue(reloaded().getHourlyRate().isEmpty());
    }

    @Test
    public void invalidRate_doesNotChangeMemoryOrFile() throws Exception {
        logic.execute(ADD + " r/45");
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String command : new String[]{"edit 1 r/-1", "edit 1 r/45.001",
            "edit 1 r/1e2", "edit 1 r/50 r/60", "edit 1 r/$50"}) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(before, model.getAddressBook());
            assertEquals(before, storage.readAddressBook().orElseThrow());
        }
        for (String suffix : new String[]{" r/", " r/-1", " r/45.001", " r/1e2", " r/50 r/60"}) {
            assertThrows(ParseException.class, () -> logic.execute(ADD.replace("Alex Tan", "Bobby") + suffix));
            assertEquals(before, model.getAddressBook());
            assertEquals(before, storage.readAddressBook().orElseThrow());
        }
    }

    private Person reloaded() throws Exception {
        return storage.readAddressBook().orElseThrow().getPersonList().getFirst();
    }
}
