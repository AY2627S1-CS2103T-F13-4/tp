package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.UiManager;

public class MainAppTest {

    /** A phone of "12" is too short, so the whole file fails to load. */
    private static final String INVALID_DATA = "{ \"persons\" : [ { \"name\" : \"Alex Yeoh\", \"phone\" : \"12\" } ] }";

    @TempDir
    public Path testFolder;

    @Test
    public void initModelManager_invalidDataFile_backsUpFileAndStartsEmpty() throws IOException {
        Path dataFile = testFolder.resolve("studentbook.json");
        Files.writeString(dataFile, INVALID_DATA);
        MainApp app = new MainApp();

        Model model = app.initModelManager(storageFor(dataFile), new UserPrefs());

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        List<Path> backups = backupsIn(testFolder);
        assertEquals(1, backups.size());
        // The next command saves over the data file, so the backup must hold the user's original bytes.
        assertArrayEquals(INVALID_DATA.getBytes(), Files.readAllBytes(backups.get(0)));
        assertFalse(app.isDataFileAtRisk);
        assertTrue(app.dataFileWarning.contains("started with no students"));
        assertTrue(app.dataFileWarning.contains(backups.get(0).toString()));
    }

    @Test
    public void initModelManager_nullDataFile_backsUpFileAndStartsEmpty() throws IOException {
        // A file of just "null" is invalid, not missing; treating it as missing would load sample data
        // and the next command would save over it without a backup.
        Path dataFile = testFolder.resolve("studentbook.json");
        Files.writeString(dataFile, "null");
        MainApp app = new MainApp();

        Model model = app.initModelManager(storageFor(dataFile), new UserPrefs());

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        List<Path> backups = backupsIn(testFolder);
        assertEquals(1, backups.size());
        assertEquals("null", Files.readString(backups.get(0)));
        assertTrue(app.dataFileWarning.contains("started with no students"));
    }

    @Test
    public void initModelManager_validDataFile_noBackupOrWarning() throws IOException {
        Path dataFile = testFolder.resolve("studentbook.json");
        Files.writeString(dataFile, "{ \"persons\" : [ ] }");
        MainApp app = new MainApp();

        app.initModelManager(storageFor(dataFile), new UserPrefs());

        assertTrue(backupsIn(testFolder).isEmpty());
        assertNull(app.dataFileWarning);
    }

    @Test
    public void initModelManager_nullListEntries_backsUpWholeFileAndStartsEmpty() throws Exception {
        String validPerson = "{\"name\":\"Test Student\",\"phone\":\"91234567\","
                + "\"email\":\"test@example.com\",\"address\":\"Test Road\"}";
        for (String field : List.of("persons", "tags", "lessons", "notes", "followUps")) {
            for (boolean archived : new boolean[] {false, true}) {
                Path folder = Files.createDirectory(testFolder.resolve(field + archived));
                Path dataFile = folder.resolve("studentbook.json");
                String invalidPerson = field.equals("persons") ? "null"
                        : validPerson.substring(0, validPerson.length() - 1)
                                + ",\"isArchived\":" + archived + ",\"" + field + "\":[null]}";
                String data = "{\"persons\":[" + validPerson + "," + invalidPerson + "]}";
                Files.writeString(dataFile, data);
                MainApp app = appWithData(dataFile);

                assertTrue(app.model.getAddressBook().getPersonList().isEmpty());
                assertTrue(app.model.getAddressBook().getArchivedPersonList().isEmpty());
                List<Path> backups = backupsIn(folder);
                assertEquals(1, backups.size());
                byte[] originalBytes = Files.readAllBytes(dataFile);
                assertArrayEquals(originalBytes, Files.readAllBytes(backups.get(0)));
                assertFalse(app.isDataFileAtRisk);
                assertTrue(app.dataFileWarning.contains(backups.get(0).toString()));
                assertTrue(app.createUi() instanceof UiManager);

                // A later successful command may save an empty book; the original must remain recoverable.
                app.logic.execute("list");
                assertArrayEquals(originalBytes, Files.readAllBytes(backups.get(0)));
            }
        }
    }

    @Test
    public void initModelManager_backupFails_closesWithoutChangingFile() throws IOException {
        // A folder in place of the data file cannot be read or backed up, like a file in a read-only folder.
        Path dataFile = Files.createDirectory(testFolder.resolve("studentbook.json"));
        MainApp app = new MainApp();

        app.initModelManager(storageFor(dataFile), new UserPrefs());

        assertTrue(app.isDataFileAtRisk);
        assertTrue(app.dataFileWarning.contains("could not make a backup copy"));
        assertTrue(backupsIn(testFolder).isEmpty());
    }

    @Test
    public void createUi_backupFailed_mainWindowNotOpened() throws IOException {
        Path dataFile = Files.createDirectory(testFolder.resolve("studentbook.json"));
        MainApp app = appWithData(dataFile);

        // The main window would let a command save over the unreadable file, so only the error is shown.
        assertFalse(app.createUi() instanceof UiManager);
    }

    @Test
    public void createUi_backupMade_mainWindowOpened() throws IOException {
        Path dataFile = testFolder.resolve("studentbook.json");
        Files.writeString(dataFile, INVALID_DATA);
        MainApp app = appWithData(dataFile);

        assertTrue(app.createUi() instanceof UiManager);
    }

    @Test
    public void backUpDataFile_folder_throwsIoException() throws IOException {
        Path dataFile = Files.createDirectory(testFolder.resolve("studentbook.json"));

        assertThrows(IOException.class, () -> MainApp.backUpDataFile(dataFile, LocalDateTime.now()));
        assertTrue(backupsIn(testFolder).isEmpty());
    }

    @Test
    public void backUpDataFile_namesBackupWithTime() throws IOException {
        Path dataFile = testFolder.resolve("studentbook.json");
        Files.writeString(dataFile, INVALID_DATA);

        Path backup = MainApp.backUpDataFile(dataFile, LocalDateTime.of(2026, 10, 28, 14, 30, 5));

        assertEquals(testFolder.resolve("studentbook-backup-20261028-143005.json"), backup);
    }

    @Test
    public void backUpDataFile_existingBackup_notReplaced() throws IOException {
        Path dataFile = testFolder.resolve("studentbook.json");
        Files.writeString(dataFile, INVALID_DATA);
        LocalDateTime time = LocalDateTime.of(2026, 10, 28, 14, 30, 5);
        Path backup = MainApp.backUpDataFile(dataFile, time);
        Files.writeString(backup, "older backup");

        assertThrows(FileAlreadyExistsException.class, () -> MainApp.backUpDataFile(dataFile, time));
        assertEquals("older backup", Files.readString(backup));
    }

    @Test
    public void initPrefs_nullGuiSettings_usesAndSavesDefaults() throws Exception {
        Path prefsFile = testFolder.resolve("preferences.json");
        Files.writeString(prefsFile, "{\"guiSettings\":null}");
        JsonUserPrefsStorage prefsStorage = new JsonUserPrefsStorage(prefsFile);

        assertEquals(new UserPrefs(), new MainApp().initPrefs(prefsStorage));
        // The defaults are saved back, so the next launch reads a valid file instead of crashing again.
        assertEquals(Optional.of(new UserPrefs()), prefsStorage.readUserPrefs());
    }

    private static MainApp appWithData(Path dataFile) {
        MainApp app = new MainApp();
        app.storage = storageFor(dataFile);
        app.model = app.initModelManager(app.storage, new UserPrefs());
        app.logic = new LogicManager(app.model, app.storage);
        return app;
    }

    private static StorageManager storageFor(Path dataFile) {
        return new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(dataFile.resolveSibling("preferences.json")));
    }

    private static List<Path> backupsIn(Path folder) throws IOException {
        try (Stream<Path> files = Files.list(folder)) {
            return files.filter(file -> file.getFileName().toString().startsWith("studentbook-backup-")).toList();
        }
    }
}
