package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Lesson;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_NONE, model);
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, temporaryFolder.resolve("ExceptionAddressBook.json")));
    }

    @Test
    public void execute_storageDeniedTemporaryFile_reportsDataFilePath() {
        IOException failure = new AccessDeniedException(
                temporaryFolder.resolve(".studentbook-123.tmp").toString());
        assertCommandFailureForExceptionFromStorage(failure, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, temporaryFolder.resolve("ExceptionAddressBook.json")));
    }

    @Test
    public void execute_subjectCapitalizationEdit_isSaved() throws Exception {
        assertCapitalizationEditSaved("lesson-edit 1 1 subject/Math", "Math", "home");
    }

    @Test
    public void execute_locationCapitalizationEdit_isSaved() throws Exception {
        assertCapitalizationEditSaved("lesson-edit 1 1 location/Home", "math", "Home");
    }

    @Test
    public void execute_subjectAndLocationCapitalizationEdit_isSaved() throws Exception {
        assertCapitalizationEditSaved("lesson-edit 1 1 subject/Math location/Home", "Math", "Home");
    }

    @Test
    public void execute_archivedStudentCapitalizationEdit_isSaved() throws Exception {
        model.addPerson(AMY);
        logic.execute("lesson-add 1 subject/math slot/Mon 1.00pm 2.00pm rate/40 location/home");
        logic.execute("archive 1");
        logic.execute("list-archive");
        logic.execute("lesson-edit 1 1 subject/Math location/Home");
        logic.execute("exit");

        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Lesson lesson = storage.readAddressBook().orElseThrow().getArchivedPersonList().get(0).getLessons().get(0);
        assertEquals("Math", lesson.getSubject().value);
        assertEquals("Home", lesson.getLocation().orElseThrow().value);
    }

    @Test
    public void execute_failedCapitalizationEdit_rollsBackAndCanBeRetried() throws Exception {
        for (IOException failure : new IOException[] {DUMMY_IO_EXCEPTION, DUMMY_AD_EXCEPTION}) {
            for (String edit : List.of("lesson-edit 1 1 subject/Math", "lesson-edit 1 1 location/Home")) {
                AtomicBoolean failSaves = new AtomicBoolean(false);
                Path dataFile = temporaryFolder.resolve("case-retry.json");
                JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile) {
                    @Override
                    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                        if (failSaves.get()) {
                            throw failure;
                        }
                        super.saveAddressBook(addressBook);
                    }
                };
                Model testModel = new ModelManager();
                testModel.addPerson(AMY);
                Logic testLogic = new LogicManager(testModel, new StorageManager(storage,
                        new JsonUserPrefsStorage(temporaryFolder.resolve("case-retry-prefs.json"))));
                testLogic.execute("lesson-add 1 subject/math slot/Mon 1.00pm 2.00pm rate/40 location/home");
                failSaves.set(true);

                CommandException error = org.junit.jupiter.api.Assertions.assertThrows(
                        CommandException.class, () -> testLogic.execute(edit));

                assertTrue(error.getMessage().contains("records are unchanged"));
                assertLessonText(testModel.getAddressBook(), "math", "home");
                assertLessonText(storage.readAddressBook().orElseThrow(), "math", "home");
                failSaves.set(false);
                testLogic.execute(edit);
                assertLessonText(storage.readAddressBook().orElseThrow(),
                        edit.contains("subject/") ? "Math" : "math", edit.contains("location/") ? "Home" : "home");
            }
        }
    }

    private void assertCapitalizationEditSaved(String edit, String subject, String location) throws Exception {
        model.addPerson(AMY);
        logic.execute("lesson-add 1 subject/math slot/Mon 1.00pm 2.00pm rate/40 location/home");
        logic.execute(edit);
        logic.execute("exit");

        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertLessonText(storage.readAddressBook().orElseThrow(), subject, location);
    }

    private static void assertLessonText(ReadOnlyAddressBook records, String subject, String location) {
        Lesson lesson = records.getPersonList().get(0).getLessons().get(0);
        assertEquals(subject, lesson.getSubject().value);
        assertEquals(location, lesson.getLocation().orElseThrow().value);
    }

    @Test
    public void execute_failedDelete_rollsBackAndRetryDeletesTheSameStudent() throws Exception {
        for (IOException failure : new IOException[] {DUMMY_IO_EXCEPTION, DUMMY_AD_EXCEPTION}) {
            Path dataFile = temporaryFolder.resolve("retry.json");
            AtomicBoolean failSaves = new AtomicBoolean(false);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(dataFile) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    if (failSaves.get()) {
                        throw failure;
                    }
                    super.saveAddressBook(addressBook);
                }
            };
            Model testModel = new ModelManager();
            testModel.addPerson(AMY);
            storage.saveAddressBook(testModel.getAddressBook());
            Logic testLogic = new LogicManager(testModel, new StorageManager(storage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("retry-prefs.json"))));
            failSaves.set(true);

            CommandException error = org.junit.jupiter.api.Assertions.assertThrows(
                    CommandException.class, () -> testLogic.execute("delete 1"));

            assertTrue(error.getMessage().contains("command was not applied"));
            assertTrue(error.getMessage().contains("records are unchanged"));
            assertEquals(AMY, testModel.getFilteredPersonList().get(0));
            assertEquals(AMY, storage.readAddressBook().orElseThrow().getPersonList().get(0));

            // Read-only commands work even while storage is unavailable, and do not persist a failed delete.
            for (String command : new String[] {"list", "find Amy", "week", "view 1", "help", "exit"}) {
                testLogic.execute(command);
            }
            assertEquals(AMY, testModel.getAddressBook().getPersonList().get(0));
            assertEquals(AMY, storage.readAddressBook().orElseThrow().getPersonList().get(0));

            failSaves.set(false);
            assertEquals(ListCommand.MESSAGE_SUCCESS, testLogic.execute("list").getFeedbackToUser());
            assertEquals(AMY, storage.readAddressBook().orElseThrow().getPersonList().get(0));
            testLogic.execute("delete 1");
            assertTrue(storage.readAddressBook().orElseThrow().getPersonList().isEmpty());
        }
    }

    // The UI counts all students from this list; it must stay the model's live list, not a copy.
    @Test
    public void getAddressBook_reflectsLaterChanges() throws Exception {
        int before = logic.getAddressBook().getPersonList().size();
        logic.execute(AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY);
        assertEquals(before + 1, logic.getAddressBook().getPersonList().size());
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path dataPath = temporaryFolder.resolve("ExceptionAddressBook.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(dataPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        ModelManager expectedModel = new ModelManager();
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
