package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.Set;

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

    @Test
    public void save_symbolicLink_updatesTargetWithoutReplacingLink() throws Exception {
        Path target = testFolder.resolve("students.json");
        new JsonAddressBookStorage(target).saveAddressBook(getTypicalAddressBook());
        Path link = testFolder.resolve("linked.json");
        try {
            Files.createSymbolicLink(link, target.getFileName());
        } catch (IOException | UnsupportedOperationException e) {
            org.junit.jupiter.api.Assumptions.abort("Symbolic links are unavailable: " + e.getClass().getSimpleName());
        }

        new JsonAddressBookStorage(link).saveAddressBook(new AddressBook());

        assertTrue(Files.isSymbolicLink(link));
        assertTrue(new JsonAddressBookStorage(target).readAddressBook().orElseThrow().getPersonList().isEmpty());
        try (var files = Files.list(testFolder)) {
            assertEquals(2, files.count());
        }
    }

    @Test
    public void copyAclPermissions_preservesEntriesAndChangesOwnerOnlyWhenNeeded() throws Exception {
        UserPrincipal owner = () -> "record-owner";
        UserPrincipal otherOwner = () -> "temporary-file-owner";
        AclEntry entry = AclEntry.newBuilder().setType(AclEntryType.ALLOW).setPrincipal(owner)
                .setPermissions(AclEntryPermission.READ_DATA, AclEntryPermission.WRITE_DATA).build();
        TestAclView source = new TestAclView(owner, List.of(entry));
        for (UserPrincipal originalOwner : List.of(owner, otherOwner)) {
            TestAclView destination = new TestAclView(originalOwner, List.of());

            JsonAddressBookStorage.copyPermissions(null, null, source, destination);

            assertSame(owner, destination.getOwner());
            assertEquals(List.of(entry), destination.getAcl());
            assertEquals(originalOwner == owner ? 0 : 1, destination.ownerChanges);
            assertEquals(List.of(entry), source.getAcl());
        }
    }

    @Test
    public void copyPermissions_posix_preservesOwnerGroupAndMode() throws Exception {
        UserPrincipal owner = () -> "record-owner";
        GroupPrincipal group = () -> "record-group";
        UserPrincipal otherOwner = () -> "temporary-file-owner";
        GroupPrincipal otherGroup = () -> "temporary-file-group";
        Set<PosixFilePermission> mode = Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
        TestPosixView source = new TestPosixView(new TestPosixAttributes(owner, group, mode));
        for (boolean sameIdentity : new boolean[] {true, false}) {
            TestPosixView destination = new TestPosixView(new TestPosixAttributes(
                    sameIdentity ? owner : otherOwner, sameIdentity ? group : otherGroup, Set.of()));

            JsonAddressBookStorage.copyPermissions(source, destination, null, null);

            assertEquals(source.readAttributes(), destination.readAttributes());
            assertEquals(sameIdentity ? 0 : 1, destination.ownerChanges);
            assertEquals(sameIdentity ? 0 : 1, destination.groupChanges);
        }
    }

    @Test
    public void copyPermissions_noAttributeViews_doesNotRequireUnsupportedAttributes() throws Exception {
        JsonAddressBookStorage.copyPermissions(null, null, null, null);
    }

    private record TestPosixAttributes(UserPrincipal owner, GroupPrincipal group, Set<PosixFilePermission> permissions)
            implements PosixFileAttributes {
        @Override
        public FileTime lastModifiedTime() {
            return FileTime.fromMillis(0);
        }

        @Override
        public FileTime lastAccessTime() {
            return FileTime.fromMillis(0);
        }

        @Override
        public FileTime creationTime() {
            return FileTime.fromMillis(0);
        }

        @Override
        public boolean isRegularFile() {
            return true;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public boolean isSymbolicLink() {
            return false;
        }

        @Override
        public boolean isOther() {
            return false;
        }

        @Override
        public long size() {
            return 0;
        }

        @Override
        public Object fileKey() {
            return null;
        }
    }

    private static class TestPosixView implements PosixFileAttributeView {
        private TestPosixAttributes attributes;
        private int ownerChanges;
        private int groupChanges;

        TestPosixView(TestPosixAttributes attributes) {
            this.attributes = attributes;
        }

        @Override
        public String name() {
            return "posix";
        }

        @Override
        public PosixFileAttributes readAttributes() {
            return attributes;
        }

        @Override
        public UserPrincipal getOwner() {
            return attributes.owner();
        }

        @Override
        public void setOwner(UserPrincipal owner) {
            attributes = new TestPosixAttributes(owner, attributes.group(), attributes.permissions());
            ownerChanges++;
        }

        @Override
        public void setGroup(GroupPrincipal group) {
            attributes = new TestPosixAttributes(attributes.owner(), group, attributes.permissions());
            groupChanges++;
        }

        @Override
        public void setPermissions(Set<PosixFilePermission> permissions) {
            attributes = new TestPosixAttributes(attributes.owner(), attributes.group(), Set.copyOf(permissions));
        }

        @Override
        public void setTimes(FileTime modified, FileTime accessed, FileTime created) {
            throw new AssertionError("Permission copying must not change timestamps.");
        }
    }

    /** Allows ACL preservation to be checked on CI systems whose filesystems expose only POSIX permissions. */
    private static class TestAclView implements AclFileAttributeView {
        private UserPrincipal owner;
        private List<AclEntry> entries;
        private int ownerChanges;

        TestAclView(UserPrincipal owner, List<AclEntry> entries) {
            this.owner = owner;
            this.entries = List.copyOf(entries);
        }

        @Override
        public String name() {
            return "acl";
        }

        @Override
        public UserPrincipal getOwner() {
            return owner;
        }

        @Override
        public void setOwner(UserPrincipal owner) {
            this.owner = owner;
            ownerChanges++;
        }

        @Override
        public List<AclEntry> getAcl() {
            return entries;
        }

        @Override
        public void setAcl(List<AclEntry> entries) {
            this.entries = List.copyOf(entries);
        }
    }
}
