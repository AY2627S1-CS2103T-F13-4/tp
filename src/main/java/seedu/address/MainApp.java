package seedu.address;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final String VERSION = "V1.5";

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);
    private static final Path USER_PREFS_FILE_PATH = Paths.get("preferences.json");
    private static final Path ADDRESS_BOOK_FILE_PATH = Paths.get("data", "studentbook.json");
    private static final DateTimeFormatter BACKUP_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model model;
    /** Tells the user at startup why the data file was not loaded, or null if it loaded. */
    protected String dataFileWarning;
    /** True if the data file could neither be loaded nor backed up, so StudentBook must close before saving. */
    protected boolean isDataFileAtRisk;

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing StudentBook ]===========================");
        super.init();

        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(USER_PREFS_FILE_PATH);
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(ADDRESS_BOOK_FILE_PATH);
        storage = new StorageManager(addressBookStorage, userPrefsStorage);

        model = initModelManager(storage, userPrefs);

        logic = new LogicManager(model, storage);

        ui = createUi();
    }

    /**
     * Returns the UI to start: an error that closes StudentBook if the data file could not be backed up,
     * so nothing saves over it, or else the main window.
     */
    Ui createUi() {
        if (isDataFileAtRisk) {
            return primaryStage -> UiManager.showStartupErrorAndExit(dataFileWarning);
        }
        return new UiManager(logic, storage.getAddressBookFilePath(), dataFileWarning,
                getHostServices()::showDocument);
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s address book and {@code userPrefs}. <br>
     * The data from the sample address book will be used instead if {@code storage}'s address book is not found,
     * or an empty address book will be used instead if errors occur when reading {@code storage}'s address book.
     * The unreadable file is copied to a backup first, since the next command saves over it.
     */
    Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        logger.info("Using data file : " + storage.getAddressBookFilePath());

        Optional<ReadOnlyAddressBook> addressBookOptional;
        ReadOnlyAddressBook initialData;
        try {
            addressBookOptional = storage.readAddressBook();
            if (addressBookOptional.isEmpty()) {
                logger.info("Creating a new data file " + storage.getAddressBookFilePath()
                        + " populated with a sample StudentBook.");
            }
            initialData = addressBookOptional.orElseGet(SampleDataUtil::getSampleAddressBook);
        } catch (DataLoadingException e) {
            Path dataFile = storage.getAddressBookFilePath();
            logger.warning("Data file at " + dataFile + " could not be loaded."
                    + " Will be starting with an empty StudentBook.");
            initialData = new AddressBook();
            try {
                Path backup = backUpDataFile(dataFile, LocalDateTime.now());
                logger.info("Backed up the data file to " + backup);
                dataFileWarning = "Could not read " + dataFile + ", so StudentBook started with no students.\n"
                        + "Your old file is kept at " + backup + ". "
                        + "Your next command saves over " + dataFile + ".\n"
                        + "To restore it, close StudentBook, fix the backup and rename it to "
                        + dataFile.getFileName() + ".";
            } catch (IOException backupError) {
                logger.severe("Failed to back up the data file : " + StringUtil.getDetails(backupError));
                isDataFileAtRisk = true;
                dataFileWarning = "Could not read " + dataFile + ", and could not make a backup copy of it.\n"
                        + "StudentBook will close so that the file is not changed. "
                        + "Check that you can open the file and save to the folder that holds it, "
                        + "or move the file somewhere else, then open StudentBook again.";
            }
        }

        return new ModelManager(initialData, userPrefs);
    }

    /**
     * Copies {@code dataFile} to a new file beside it whose name has {@code time}, such as
     * {@code studentbook-backup-20261028-143005.json}, and returns the copy's path.
     * The time keeps each backup separate, and an existing file is never replaced.
     *
     * @throws IOException if {@code dataFile} is not a file, e.g. a folder, or the copy fails.
     */
    static Path backUpDataFile(Path dataFile, LocalDateTime time) throws IOException {
        if (!Files.isRegularFile(dataFile)) {
            // Copying a folder would only make an empty folder, not a backup.
            throw new IOException(dataFile + " is not a file");
        }
        String fileName = dataFile.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        int extensionStart = dot > 0 ? dot : fileName.length();
        String backupName = fileName.substring(0, extensionStart) + "-backup-" + time.format(BACKUP_TIME_FORMAT)
                + fileName.substring(extensionStart);
        return Files.copy(dataFile, dataFile.resolveSibling(backupName));
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(JsonUserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using preference file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            if (prefsOptional.isEmpty()) {
                logger.info("Creating new preference file " + prefsFilePath);
            }
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataLoadingException e) {
            logger.warning("Preference file at " + prefsFilePath + " could not be loaded."
                    + " Using default preferences.");
            initializedPrefs = new UserPrefs();
        }

        //Update prefs file in case it was missing to begin with or there are new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save preference file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting StudentBook " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping StudentBook ] =============================");
        try {
            storage.saveUserPrefs(model.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
