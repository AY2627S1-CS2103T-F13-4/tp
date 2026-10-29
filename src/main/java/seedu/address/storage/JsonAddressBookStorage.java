package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    private Path filePath;

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        Optional<JsonSerializableAddressBook> jsonAddressBook = JsonUtil.readJsonFile(
                filePath, JsonSerializableAddressBook.class);
        if (!jsonAddressBook.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonAddressBook.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath);
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        Path target = filePath.toAbsolutePath().normalize();
        if (Files.isSymbolicLink(target)) {
            target = target.toRealPath();
        }
        if (Files.exists(target) && !Files.isWritable(target)) {
            throw new AccessDeniedException(target.toString());
        }
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(addressBook));
        Files.createDirectories(target.getParent());
        Path temporaryFile = Files.createTempFile(target.getParent(), ".studentbook-", ".tmp");
        try {
            writeTemporaryFile(temporaryFile, json);
            copyPermissions(target, temporaryFile);
            replaceFile(temporaryFile, target);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    /** Writes only to the disposable file, so a partial write cannot truncate saved records. */
    void writeTemporaryFile(Path temporaryFile, String json) throws IOException {
        Files.writeString(temporaryFile, json, StandardCharsets.UTF_8);
    }

    /** Fails safely if atomic replacement is unavailable; there is no direct-overwrite fallback. */
    void replaceFile(Path temporaryFile, Path target) throws IOException {
        Files.move(temporaryFile, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void copyPermissions(Path source, Path destination) throws IOException {
        if (!Files.exists(source)) {
            return;
        }
        copyPermissions(Files.getFileAttributeView(source, PosixFileAttributeView.class),
                Files.getFileAttributeView(destination, PosixFileAttributeView.class),
                Files.getFileAttributeView(source, AclFileAttributeView.class),
                Files.getFileAttributeView(destination, AclFileAttributeView.class));
    }

    /** Copies the supported permissions without depending on the host's filesystem type. */
    static void copyPermissions(PosixFileAttributeView sourcePosix, PosixFileAttributeView targetPosix,
            AclFileAttributeView sourceAcl, AclFileAttributeView targetAcl) throws IOException {
        if (sourcePosix != null) {
            PosixFileAttributes attributes = sourcePosix.readAttributes();
            if (!targetPosix.readAttributes().owner().equals(attributes.owner())) {
                targetPosix.setOwner(attributes.owner());
            }
            if (!targetPosix.readAttributes().group().equals(attributes.group())) {
                targetPosix.setGroup(attributes.group());
            }
            targetPosix.setPermissions(attributes.permissions());
        } else if (sourceAcl != null) {
            copyAclPermissions(sourceAcl, targetAcl);
        }
    }

    /** Copies ACL permissions independently of the host filesystem's attribute support. */
    static void copyAclPermissions(AclFileAttributeView source, AclFileAttributeView destination) throws IOException {
        if (!destination.getOwner().equals(source.getOwner())) {
            destination.setOwner(source.getOwner());
        }
        destination.setAcl(source.getAcl());
    }
}
