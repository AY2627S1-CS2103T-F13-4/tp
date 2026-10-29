package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.RateSortOrder;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Checks save failures through real parsers, records, filters and storage. */
public class SaveFailureWorkflowTest {
    @TempDir
    public Path testFolder;

    @Test
    public void modifyingCommands_saveFails_restoreRecordsFiltersAndSorting() throws Exception {
        List<String> commands = List.of("delete 1", "edit 1 name/Zed", "archive 1", "unarchive 1", "clear",
                "add name/New Student phone/91234567 email/new@example.com address/New Road",
                "guardian-edit 1 name/Other Guardian", "guardian-delete 1",
                "lesson-edit 1 1 rate/60", "lesson-delete 1 1",
                "lesson-add 1 subject/English slot/Tue 1.00pm 2.00pm rate/40",
                "note-add 1 date/2026-10-01 text/New note", "note-delete 1 1",
                "followup-add 1 text/New task", "followup-edit 1 1 text/Changed task", "followup-done 1 1");
        AddressBookParser parser = new AddressBookParser();
        for (String command : commands) {
            ModelManager model = new ModelManager();
            for (String name : List.of("Alpha", "Bravo", "Charlie", "Delta")) {
                parser.parseCommand("add name/" + name
                        + " phone/91234567 email/test@example.com address/Test Road").execute(model);
            }
            for (String setup : List.of("guardian-add 1 name/Guardian phone/81234567",
                    "lesson-add 1 subject/Math slot/Mon 1.00pm 2.00pm rate/40",
                    "note-add 1 date/2026-10-01 text/Original note", "followup-add 1 text/Original task",
                    "archive 2", "archive 3", "list-archive", "find Bravo", "list",
                    "find subject/math day/mon")) {
                parser.parseCommand(setup).execute(model);
            }
            model.setRateSortOrder(RateSortOrder.DESCENDING);
            boolean archived = command.startsWith("unarchive") || command.startsWith("add ");
            model.setArchiveView(archived);
            AddressBook original = new AddressBook(model.getAddressBook());
            var active = List.copyOf(model.getFilteredActivePersonList());
            var archive = List.copyOf(model.getFilteredArchivedPersonList());
            var lessonFilter = model.getLessonPredicate().getValue();
            Path dataFile = testFolder.resolve("students.json");
            new JsonAddressBookStorage(dataFile).saveAddressBook(original);
            byte[] originalBytes = Files.readAllBytes(dataFile);
            JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(dataFile) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook data) throws IOException {
                    throw new IOException("Test save failure");
                }
            };
            Logic logic = new LogicManager(model, new StorageManager(failingStorage,
                    new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));

            assertThrows(CommandException.class, () -> logic.execute(command), command);

            assertEquals(original, model.getAddressBook(), command);
            assertEquals(active, model.getFilteredActivePersonList(), command);
            assertEquals(archive, model.getFilteredArchivedPersonList(), command);
            assertEquals(archived, model.isArchiveView(), command);
            assertEquals(RateSortOrder.DESCENDING, model.getRateSortOrder().getValue(), command);
            assertSame(lessonFilter, model.getLessonPredicate().getValue(), command);
            assertArrayEquals(originalBytes, Files.readAllBytes(dataFile), command);
        }
    }
}
