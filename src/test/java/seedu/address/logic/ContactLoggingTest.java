package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.LogCapture;

/** Verifies diagnostic privacy through command parsing, execution, model initialization and loading. */
public class ContactLoggingTest {
    private static final List<String> PRIVATE_VALUES = List.of("Student Secret", "91234567",
            "private@example.com", "Secret Road", "Guardian Secret", "87654321");
    private static final String ADD_COMMAND = "add name/Student Secret phone/91234567"
            + " email/private@example.com address/Secret Road";
    private static final String GUARDIAN_ARGUMENTS = " name/Guardian Secret phone/87654321";

    @TempDir
    public Path temporaryFolder;

    @Test
    public void commands_logTypesAndOutcomesWithoutPrivateValues() throws Exception {
        Logic logic = createLogic();
        try (LogCapture logs = new LogCapture()) {
            CommandResult added = logic.execute(ADD_COMMAND);
            assertTrue(added.getFeedbackToUser().contains("91234567"));
            logic.execute("guardian-add 1" + GUARDIAN_ARGUMENTS);
            logic.execute("find guardian/87654321");
            logic.execute("list");
            assertThrows(CommandException.class, () -> logic.execute("guardian-add 999" + GUARDIAN_ARGUMENTS));
            assertThrows(ParseException.class, () -> logic.execute(ADD_COMMAND.replace("91234567", "invalid")));
            // An unknown command word can itself be a contact detail.
            assertThrows(ParseException.class, () -> logic.execute("private@example.com 91234567"));

            assertTrue(logs.text().contains("Command completed: AddCommand (success)"));
            assertTrue(logs.text().contains("Command completed: GuardianAddCommand (success)"));
            assertTrue(logs.text().contains("Command failed: GuardianAddCommand"));
            assertTrue(logs.text().contains("Command rejected: invalid input"));
            assertPrivateValuesAbsent(logs.text());
        }
    }

    @Test
    public void saveFailure_logsOutcomeWithoutExceptionMessage() throws Exception {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("students.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("Could not save private@example.com");
            }
        };
        Logic logic = new LogicManager(new ModelManager(), new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        try (LogCapture logs = new LogCapture()) {
            CommandException error = assertThrows(CommandException.class, () -> logic.execute(ADD_COMMAND));

            assertTrue(error.getMessage().contains("Could not save private@example.com"));
            assertTrue(logs.text().contains("Command failed: AddCommand"));
            assertFalse(logs.text().contains("Command completed"));
            assertPrivateValuesAbsent(logs.text());
        }
    }

    @Test
    public void modelInitialization_doesNotLogSavedContacts() throws Exception {
        Logic logic = createLogic();
        logic.execute(ADD_COMMAND);
        logic.execute("guardian-add 1" + GUARDIAN_ARGUMENTS);
        try (LogCapture logs = new LogCapture()) {
            new ModelManager(logic.getAddressBook(), new UserPrefs());

            assertTrue(logs.text().contains("Initializing model"));
            assertPrivateValuesAbsent(logs.text());
        }
    }

    @Test
    public void malformedJson_logsFailureWithoutJsonSource() throws Exception {
        Path dataFile = temporaryFolder.resolve("malformed.json");
        Files.writeString(dataFile, "{\"persons\":[{\"name\":\"Student Secret\",\"phone\":\"91234567\","
                + "\"email\":\"private@example.com\",\"address\":\"Secret Road\"");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile);
        try (LogCapture logs = new LogCapture()) {
            assertThrows(DataLoadingException.class, storage::readAddressBook);

            assertTrue(logs.text().contains("Error reading from jsonFile"));
            assertPrivateValuesAbsent(logs.text());
        }
    }

    private Logic createLogic() {
        return new LogicManager(new ModelManager(), new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("students.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    private static void assertPrivateValuesAbsent(String logs) {
        for (String value : PRIVATE_VALUES) {
            assertFalse(logs.contains(value), "Diagnostic logs must omit " + value);
        }
    }
}
