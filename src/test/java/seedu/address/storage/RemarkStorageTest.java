package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class RemarkStorageTest {
    @TempDir
    public Path tempDir;

    @Test
    public void saveAndReload_preservesRemark() throws Exception {
        Person person = new PersonBuilder(ALICE).withRemark("Likes 日本語!").build();
        AddressBook book = new AddressBook();
        book.addPerson(person);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(tempDir.resolve("contacts.json"));
        storage.saveAddressBook(book);
        assertEquals(book, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void oldJsonWithoutRemark_loadsWithEmptyRemark() throws Exception {
        String json = "{\"name\":\"Alice\",\"phone\":\"12345678\",\"email\":\"alice@example.com\","
                + "\"address\":\"1 Main Street\",\"tags\":[]}";
        Person person = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
        assertEquals("", person.getRemark().value);
    }

    @Test
    public void remark_changesEqualityButNotIdentity() {
        Person edited = new PersonBuilder(ALICE).withRemark("A note").build();
        assertTrue(ALICE.isSamePerson(edited));
        assertFalse(ALICE.equals(edited));
    }
}
