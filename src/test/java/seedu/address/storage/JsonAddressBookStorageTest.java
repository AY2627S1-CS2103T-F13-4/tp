package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }

    @Test
    public void save_partialTemporaryWrite_preservesOriginalAndRemovesTemporaryFile() throws Exception {
        Path target = testFolder.resolve("students.json");
        new JsonAddressBookStorage(target).saveAddressBook(getTypicalAddressBook());
        byte[] original = Files.readAllBytes(target);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(target) {
            @Override
            void writeTemporaryFile(Path temporaryFile, String json) throws IOException {
                Files.writeString(temporaryFile, "partial JSON");
                throw new IOException("Test partial write");
            }
        };

        assertThrows(IOException.class, () -> failingStorage.saveAddressBook(new AddressBook()));

        assertArrayEquals(original, Files.readAllBytes(target));
        assertOnlyTargetRemains(target);
    }

    @Test
    public void save_atomicMoveUnsupported_preservesOriginalAndRemovesTemporaryFile() throws Exception {
        Path target = testFolder.resolve("students.json");
        new JsonAddressBookStorage(target).saveAddressBook(getTypicalAddressBook());
        byte[] original = Files.readAllBytes(target);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(target) {
            @Override
            void replaceFile(Path temporaryFile, Path destination) throws IOException {
                throw new AtomicMoveNotSupportedException(temporaryFile.toString(), destination.toString(), "Test");
            }
        };

        assertThrows(AtomicMoveNotSupportedException.class, () -> failingStorage.saveAddressBook(new AddressBook()));

        assertArrayEquals(original, Files.readAllBytes(target));
        assertOnlyTargetRemains(target);
    }

    @Test
    public void save_newFileReplacementFails_doesNotLeaveEmptyDataFile() throws Exception {
        Path target = testFolder.resolve("students.json");
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(target) {
            @Override
            void replaceFile(Path temporaryFile, Path destination) throws IOException {
                throw new IOException("Test replacement failure");
            }
        };

        assertThrows(IOException.class, () -> failingStorage.saveAddressBook(getTypicalAddressBook()));

        assertFalse(Files.exists(target));
        try (var files = Files.list(testFolder)) {
            assertTrue(files.findAny().isEmpty());
        }
    }

    @Test
    public void save_existingFile_preservesPermissions() throws Exception {
        Path target = testFolder.resolve("students.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(target);
        storage.saveAddressBook(getTypicalAddressBook());
        PosixFileAttributeView posix = Files.getFileAttributeView(target, PosixFileAttributeView.class);
        AclFileAttributeView acl = Files.getFileAttributeView(target, AclFileAttributeView.class);
        var permissions = posix == null ? null : posix.readAttributes().permissions();
        var entries = acl == null ? null : acl.getAcl();

        storage.saveAddressBook(new AddressBook());

        if (posix != null) {
            assertEquals(permissions, posix.readAttributes().permissions());
        }
        if (acl != null) {
            assertEquals(entries, acl.getAcl());
        }
        assertOnlyTargetRemains(target);
    }

    @Test
    public void save_readOnlyFile_rejectsReplacementAndKeepsOriginal() throws Exception {
        Path target = testFolder.resolve("students.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(target);
        storage.saveAddressBook(getTypicalAddressBook());
        byte[] original = Files.readAllBytes(target);
        assertTrue(target.toFile().setWritable(false, false));
        try {
            org.junit.jupiter.api.Assumptions.assumeFalse(Files.isWritable(target),
                    "This account can still write read-only files");
            assertThrows(AccessDeniedException.class, () -> storage.saveAddressBook(new AddressBook()));
            assertArrayEquals(original, Files.readAllBytes(target));
            assertOnlyTargetRemains(target);
        } finally {
            assertTrue(target.toFile().setWritable(true, true));
        }
    }

    private void assertOnlyTargetRemains(Path target) throws IOException {
        try (var files = Files.list(testFolder)) {
            assertEquals(List.of(target), files.toList());
        }
    }
}
