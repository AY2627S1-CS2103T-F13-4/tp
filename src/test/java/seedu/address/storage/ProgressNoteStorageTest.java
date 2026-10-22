package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.Student;
import seedu.address.testutil.PersonBuilder;

/** Checks that notes survive saving and loading, and that older files without notes still load. */
public class ProgressNoteStorageTest {
    private static final String LEGACY_PERSON = "{\"name\":\"Alex Tan\",\"phone\":\"91234567\","
            + "\"email\":\"alex@example.com\",\"address\":\"Clementi\"}";

    private static String withNotes(String notesJson) {
        return LEGACY_PERSON.substring(0, LEGACY_PERSON.length() - 1) + ",\"notes\":" + notesJson + "}";
    }

    private static Student load(String json) throws Exception {
        return JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
    }

    @Test
    public void notes_roundTripOldestFirst() throws Exception {
        ProgressNote later = new ProgressNote(LocalDate.of(2026, 10, 15), "Fractions, and/or decimals next");
        ProgressNote earlier = new ProgressNote(LocalDate.of(2026, 9, 1), "Placement test");
        ProgressNote sameDayAddedFirst = new ProgressNote(LocalDate.of(2026, 9, 1), "Set homework");
        Student original = new PersonBuilder().withNotes(later, sameDayAddedFirst, earlier).build();

        String saved = JsonUtil.toJsonString(new JsonAdaptedPerson(original));
        assertTrue(saved.contains("\"notes\""), saved);
        assertTrue(saved.contains("\"2026-09-01\""), saved);
        Student loaded = load(saved);
        assertEquals(original, loaded);
        assertEquals(List.of(sameDayAddedFirst, earlier, later), loaded.getNotes());
    }

    @Test
    public void missingNullOrEmptyNotes_loadAsNoNotes() throws Exception {
        for (String json : List.of(LEGACY_PERSON, withNotes("null"), withNotes("[]"))) {
            assertTrue(load(json).getNotes().isEmpty(), json);
        }
    }

    @Test
    public void invalidNotes_rejected() {
        List<String> invalid = List.of(
                "[{\"date\":\"2026-02-30\",\"text\":\"Fractions\"}]",
                "[{\"date\":\"15/10/2026\",\"text\":\"Fractions\"}]",
                "[{\"text\":\"Fractions\"}]",
                "[{\"date\":\"2026-10-15\"}]",
                "[{\"date\":\"2026-10-15\",\"text\":\"   \"}]",
                "[{\"date\":\"2026-10-15\",\"text\":\"" + "a".repeat(501) + "\"}]",
                "[null]");
        for (String notes : invalid) {
            assertThrows(IllegalValueException.class, () -> load(withNotes(notes)), notes);
        }
    }

    @Test
    public void nonStringNoteFields_rejectedBeforeCoercion() {
        for (String notes : List.of("[{\"date\":20261015,\"text\":\"Fractions\"}]",
                "[{\"date\":\"2026-10-15\",\"text\":42}]")) {
            assertThrows(Exception.class, () -> load(withNotes(notes)), notes);
        }
    }
}
