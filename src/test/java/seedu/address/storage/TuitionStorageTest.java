package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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
import seedu.address.model.person.Guardian;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.School;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.PersonBuilder;

/** Exercises JSON, construction, identity and edit preservation together. */
public class TuitionStorageTest {
    private static final String LEGACY_PERSON = "{\"name\":\"Alex Tan\",\"phone\":\"91234567\","
            + "\"email\":\"alex@example.com\",\"address\":\"Clementi\",\"tags\":[\"student\"]}";
    private static final String VALID_SLOT = "{\"day\":\"MONDAY\",\"start\":\"16:00\",\"end\":\"17:30\"}";
    private static final String VALID_LESSON = "{\"subject\":\"Math\",\"weeklyLessonSlot\":" + VALID_SLOT
            + ",\"hourlyRate\":\"45.50\"}";

    @TempDir
    public Path temporaryFolder;

    private static Lesson lesson(String subject, DayOfWeek day, String rate) {
        return new Lesson(new Subject(subject), new WeeklyLessonSlot(day, LocalTime.of(16, 0), LocalTime.of(17, 30)),
                new HourlyRate(new BigDecimal(rate)));
    }

    private Student completeStudent() {
        return new PersonBuilder()
                .withLessons(lesson("Combined Science", DayOfWeek.MONDAY, "45.50"),
                        lesson("Math", DayOfWeek.WEDNESDAY, "50"))
                .withSchool(new School("Nanyang Primary School"))
                .withSchoolingLevel(new SchoolingLevel("Primary 5"))
                .withGuardian(new Guardian(new Name("Janet Tan"), new Phone("91234567"))).build();
    }

    private static String withProperty(String property) {
        return LEGACY_PERSON.substring(0, LEGACY_PERSON.length() - 1) + "," + property + "}";
    }

    @Test
    public void personWithoutLessons_editSaveReload_loadsAsZeroLessons() throws Exception {
        String json = "{\"persons\":[" + LEGACY_PERSON + "," + LEGACY_PERSON.replace("Alex Tan", "alex tan") + "]}";
        AddressBook book = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
        Model model = new ModelManager(book, new UserPrefs());
        new AddressBookParser().parseCommand("edit 1 phone/87654321").execute(model);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("studentbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), loaded);
        assertEquals(2, loaded.getPersonList().size());
        Student first = loaded.getPersonList().get(0);
        assertTrue(first.getLessons().isEmpty());
        assertTrue(first.getSchool().isEmpty());
        assertTrue(first.getSchoolingLevel().isEmpty());
        assertTrue(first.getGuardian().isEmpty());
    }

    @Test
    public void completeStudent_editOtherFieldAndReload_preservesLessonsInOrder() throws Exception {
        Student original = completeStudent();
        AddressBook book = new AddressBook();
        book.addPerson(original);
        Model model = new ModelManager(book, new UserPrefs());
        new AddressBookParser().parseCommand("edit 1 address/New address").execute(model);
        Student expected = new PersonBuilder(original).withAddress("New address").build();
        assertEquals(expected, model.getFilteredPersonList().get(0));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("studentbook.json"));
        storage.saveAddressBook(model.getAddressBook());
        Student reloaded = storage.readAddressBook().orElseThrow().getPersonList().get(0);
        assertEquals(expected, reloaded);
        assertEquals(original.getLessons(), reloaded.getLessons());
    }

    @Test
    public void lessonsJson_roundTripsAndNullOrEmptyMeansNoLessons() throws Exception {
        Student original = completeStudent();
        String saved = JsonUtil.toJsonString(new JsonAdaptedPerson(original));
        assertTrue(saved.contains("\"lessons\""));
        assertEquals(original, JsonUtil.fromJsonString(saved, JsonAdaptedPerson.class).toModelType());

        for (String value : new String[]{"[]", "null"}) {
            assertTrue(JsonUtil.fromJsonString(withProperty("\"lessons\":" + value), JsonAdaptedPerson.class)
                    .toModelType().getLessons().isEmpty());
        }
        Student single = JsonUtil.fromJsonString(withProperty("\"lessons\":[" + VALID_LESSON + "]"),
                JsonAdaptedPerson.class).toModelType();
        assertEquals(List.of(lesson("Math", DayOfWeek.MONDAY, "45.5")), single.getLessons());
    }

    @Test
    public void lessonLocationAndMode_roundTripAndAreOptionalInJson() throws Exception {
        Lesson withExtras = new Lesson(new Subject("Math"),
                new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(16, 0), LocalTime.of(17, 30)),
                new HourlyRate(new BigDecimal("45.50")), Optional.of(new LessonLocation("Clementi Library")),
                Optional.of(LessonMode.PHYSICAL));
        String saved = JsonUtil.toJsonString(new JsonAdaptedLesson(withExtras));
        assertTrue(saved.contains("\"mode\" : \"PHYSICAL\""));
        assertEquals(withExtras, JsonUtil.fromJsonString(saved, JsonAdaptedLesson.class).toModelType());

        String plain = JsonUtil.toJsonString(new JsonAdaptedLesson(lesson("Math", DayOfWeek.MONDAY, "45.50")));
        assertFalse(plain.contains("location"));
        assertFalse(plain.contains("mode"));

        Lesson loaded = JsonUtil.fromJsonString(VALID_LESSON.replace("}}", "}").replaceAll("\\}$",
                ",\"mode\":\"virtual\",\"location\":null}"), JsonAdaptedLesson.class).toModelType();
        assertEquals(Optional.of(LessonMode.VIRTUAL), loaded.getMode());
        assertEquals(Optional.empty(), loaded.getLocation());
        for (String bad : new String[]{",\"mode\":\"hybrid\"", ",\"location\":\"\"", ",\"mode\":1"}) {
            String json = VALID_LESSON.replaceAll("\\}$", bad + "}");
            assertThrows(Exception.class, () -> JsonUtil.fromJsonString(json, JsonAdaptedLesson.class)
                    .toModelType(), json);
        }
    }

    @Test
    public void duplicateLessons_areRejected() {
        String json = withProperty("\"lessons\":[" + VALID_LESSON + "," + VALID_LESSON + "]");
        IllegalValueException e = assertThrows(IllegalValueException.class, () ->
                JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
        assertEquals(Student.MESSAGE_DUPLICATE_LESSONS, e.getMessage());
    }

    @Test
    public void invalidLessonJson_rejectedWithoutInventingValues() {
        String badSlot = "\"weeklyLessonSlot\":";
        for (String lesson : new String[]{
            "{\"subject\":\"\",\"weeklyLessonSlot\":" + VALID_SLOT + ",\"hourlyRate\":\"45\"}",
            "{\"subject\":\"123\",\"weeklyLessonSlot\":" + VALID_SLOT + ",\"hourlyRate\":\"45\"}",
            "{\"weeklyLessonSlot\":" + VALID_SLOT + ",\"hourlyRate\":\"45\"}",
            "{\"subject\":\"Math\",\"hourlyRate\":\"45\"}",
            "{\"subject\":\"Math\"," + badSlot + VALID_SLOT + "}",
            "{\"subject\":\"Math\"," + badSlot + VALID_SLOT + ",\"hourlyRate\":\"-1\"}",
            "{\"subject\":\"Math\"," + badSlot + VALID_SLOT + ",\"hourlyRate\":\"1.001\"}",
            "{\"subject\":\"Math\"," + badSlot + VALID_SLOT + ",\"hourlyRate\":\"1e2\"}",
            "{\"subject\":\"Math\"," + badSlot + "{\"day\":\"MONDAY\",\"start\":\"24:00\",\"end\":\"25:00\"},"
                    + "\"hourlyRate\":\"45\"}",
            "{\"subject\":\"Math\"," + badSlot + "{\"day\":\"MONDAY\",\"start\":\"18:00\",\"end\":\"17:00\"},"
                    + "\"hourlyRate\":\"45\"}",
            "{\"subject\":\"Math\"," + badSlot + "{\"day\":\"MONDAY\",\"start\":\"17:00\",\"end\":\"17:00\"},"
                    + "\"hourlyRate\":\"45\"}",
            "{\"subject\":\"Math\"," + badSlot + "{\"start\":\"16:00\",\"end\":\"17:00\"},\"hourlyRate\":\"45\"}",
            "{\"subject\":\"Math\"," + badSlot + "{\"day\":\"MONDAY\",\"end\":\"17:00\"},\"hourlyRate\":\"45\"}",
            "{\"subject\":\"Math\"," + badSlot + "{\"day\":\"MONDAY\",\"start\":\"16:00\"},\"hourlyRate\":\"45\"}"}) {
            String json = withProperty("\"lessons\":[" + lesson + "]");
            assertThrows(IllegalValueException.class, () -> {
                JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
            }, lesson);
        }
    }

    @Test
    public void invalidOtherTuitionJson_rejected() {
        for (String property : new String[]{"\"schoolingLevel\":\"123\"",
            "\"guardianContact\":{\"name\":\"Janet\"}"}) {
            String json = withProperty(property);
            assertThrows(IllegalValueException.class, () -> {
                JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
            }, property);
        }
    }

    @Test
    public void explicitNullNewFields_loadAsAbsent() throws Exception {
        String json = withProperty("\"schoolingLevel\":null,\"guardianContact\":null,\"lessons\":null");
        assertEquals(JsonUtil.fromJsonString(LEGACY_PERSON, JsonAdaptedPerson.class).toModelType(),
                JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void nonStringTuitionJson_rejectedBeforeCoercion() {
        for (String property : new String[]{"\"schoolingLevel\":false",
            "\"guardianContact\":{\"name\":true,\"phone\":1234}",
            "\"lessons\":[{\"subject\":true,\"weeklyLessonSlot\":" + VALID_SLOT + ",\"hourlyRate\":\"45\"}]",
            "\"lessons\":[{\"subject\":\"Math\",\"weeklyLessonSlot\":" + VALID_SLOT + ",\"hourlyRate\":45.5}]",
            "\"lessons\":[{\"subject\":\"Math\",\"weeklyLessonSlot\":{\"day\":true,\"start\":1600,"
                    + "\"end\":1700},\"hourlyRate\":\"45\"}]"}) {
            String json = withProperty(property);
            assertThrows(java.io.IOException.class, () -> {
                JsonUtil.fromJsonString(json, JsonAdaptedPerson.class);
            }, property);
        }
    }

}
