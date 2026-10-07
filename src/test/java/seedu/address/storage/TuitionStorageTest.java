package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.GuardianContact;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.PersonBuilder;

/** Exercises JSON, construction, identity and edit preservation together. */
public class TuitionStorageTest {
    private static final String LEGACY_PERSON = "{\"name\":\"Alex Tan\",\"phone\":\"91234567\","
            + "\"email\":\"alex@example.com\",\"address\":\"Clementi\",\"tags\":[\"student\"]}";

    @TempDir
    public Path temporaryFolder;

    private Person completeStudent() {
        return new PersonBuilder().withSubject(new Subject("Combined Science"))
                .withSchoolingLevel(new SchoolingLevel("Primary 5"))
                .withGuardianContact(new GuardianContact(new Name("Janet Tan"), new Phone("91234567")))
                .withWeeklyLessonSlot(new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(16, 0), LocalTime.of(17, 30)))
                .withHourlyRate(new HourlyRate(new BigDecimal("45.50"))).build();
    }

    @Test
    public void legacyLoad_editSaveReload_preservesOldFieldsAndMissingValues() throws Exception {
        String json = "{\"persons\":[" + LEGACY_PERSON + "," + LEGACY_PERSON.replace("Alex Tan", "alex tan") + "]}";
        AddressBook book = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
        Model model = new ModelManager(book, new UserPrefs());
        new AddressBookParser().parseCommand("edit 1 p/87654321").execute(model);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), loaded);
        assertEquals(2, loaded.getPersonList().size());
        Person first = loaded.getPersonList().get(0);
        assertTrue(first.getSubject().isEmpty());
        assertTrue(first.getSchoolingLevel().isEmpty());
        assertTrue(first.getGuardianContact().isEmpty());
        assertTrue(first.getWeeklyLessonSlot().isEmpty());
        assertTrue(first.getHourlyRate().isEmpty());
    }

    @Test
    public void completeStudent_editOldFieldAndReload_preservesAllTuitionValues() throws Exception {
        Person original = completeStudent();
        AddressBook book = new AddressBook();
        book.addPerson(original);
        Model model = new ModelManager(book, new UserPrefs());
        new AddressBookParser().parseCommand("edit 1 a/New address").execute(model);
        Person expected = new PersonBuilder(original).withAddress("New address").build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        assertEquals(expected, storage.readAddressBook().orElseThrow().getPersonList().get(0));
        assertNotEquals(original, new PersonBuilder(original).withSubject(null).build());
        assertTrue(original.isSamePerson(new PersonBuilder(original).withSubject(null).build()));
    }

    @Test
    public void invalidTuitionJson_rejectedWithoutInventingValues() {
        for (String property : new String[]{"\"subject\":\"\"", "\"schoolingLevel\":\"123\"",
            "\"guardianContact\":{\"name\":\"Janet\"}",
            "\"weeklyLessonSlot\":{\"day\":\"MONDAY\",\"start\":\"24:00\",\"end\":\"25:00\"}",
            "\"weeklyLessonSlot\":{\"day\":\"MONDAY\",\"start\":\"18:00\",\"end\":\"17:00\"}",
            "\"hourlyRate\":\"-1\"", "\"hourlyRate\":\"1.001\"", "\"hourlyRate\":\"1e2\""}) {
            String json = LEGACY_PERSON.substring(0, LEGACY_PERSON.length() - 1) + "," + property + "}";
            assertThrows(IllegalValueException.class, () -> {
                JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
            }, property);
        }
    }

    @Test
    public void explicitNullNewFields_loadAsAbsent() throws Exception {
        String json = LEGACY_PERSON.substring(0, LEGACY_PERSON.length() - 1)
                + ",\"subject\":null,\"schoolingLevel\":null,\"guardianContact\":null,"
                + "\"weeklyLessonSlot\":null,\"hourlyRate\":null}";
        assertEquals(JsonUtil.fromJsonString(LEGACY_PERSON, JsonAdaptedPerson.class).toModelType(),
                JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void nonStringTuitionJson_rejectedBeforeCoercion() {
        for (String property : new String[]{"\"subject\":true", "\"schoolingLevel\":false", "\"hourlyRate\":45.5",
            "\"guardianContact\":{\"name\":true,\"phone\":1234}",
            "\"weeklyLessonSlot\":{\"day\":true,\"start\":1600,\"end\":1700}"}) {
            String json = LEGACY_PERSON.substring(0, LEGACY_PERSON.length() - 1) + "," + property + "}";
            assertThrows(java.io.IOException.class, () -> {
                JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
            }, property);
        }
    }

}
