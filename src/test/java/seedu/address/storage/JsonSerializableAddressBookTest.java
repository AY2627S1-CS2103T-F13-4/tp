package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicateAcrossArchiveStatuses_throwsIllegalValueException() {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(new JsonAdaptedPerson(ALICE),
                new JsonAdaptedPerson(ALICE.withArchived(true))));

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                data::toModelType);
    }

    @Test
    public void toModelType_mixedArchiveStatuses_separatesLists() throws Exception {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(
                new JsonAdaptedPerson(ALICE.withArchived(true)), new JsonAdaptedPerson(BENSON)));

        AddressBook addressBook = data.toModelType();

        assertEquals(List.of(BENSON), addressBook.getPersonList());
        assertEquals(List.of(ALICE.withArchived(true)), addressBook.getArchivedPersonList());
    }

    @Test
    public void jsonRoundTrip_activeAndArchivedPersons_preservesBothLists() throws Exception {
        AddressBook original = new AddressBook();
        original.addPerson(ALICE);
        original.addPerson(BENSON.withArchived(true));

        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(original));
        AddressBook reloaded = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();

        assertEquals(original, reloaded);
    }

}
