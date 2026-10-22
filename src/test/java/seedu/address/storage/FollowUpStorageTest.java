package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Student;
import seedu.address.testutil.PersonBuilder;

/** Checks that follow-ups survive saving and loading, and that older files without them still load. */
public class FollowUpStorageTest {
    private static final String LEGACY_PERSON = "{\"name\":\"Alex Tan\",\"phone\":\"91234567\","
            + "\"email\":\"alex@example.com\",\"address\":\"Clementi\"}";

    private static String withFollowUps(String followUpsJson) {
        return LEGACY_PERSON.substring(0, LEGACY_PERSON.length() - 1) + ",\"followUps\":" + followUpsJson + "}";
    }

    private static Student load(String json) throws Exception {
        return JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
    }

    @Test
    public void followUps_roundTripInOrder() throws Exception {
        Student original = new PersonBuilder()
                .withFollowUps("Send report", "Ask about P5 and/or P6 books").build();

        String saved = JsonUtil.toJsonString(new JsonAdaptedPerson(original));
        assertTrue(saved.contains("\"followUps\""), saved);
        Student loaded = load(saved);
        assertEquals(original, loaded);
        assertEquals(List.of(new FollowUp("Send report"), new FollowUp("Ask about P5 and/or P6 books")),
                loaded.getFollowUps());
    }

    @Test
    public void missingNullOrEmptyFollowUps_loadAsNone() throws Exception {
        for (String json : List.of(LEGACY_PERSON, withFollowUps("null"), withFollowUps("[]"))) {
            assertTrue(load(json).getFollowUps().isEmpty(), json);
        }
    }

    @Test
    public void invalidFollowUps_rejected() {
        List<String> invalid = List.of(
                "[{}]",
                "[{\"text\":\"   \"}]",
                "[{\"text\":\"" + "a".repeat(FollowUp.MAX_TEXT_LENGTH + 1) + "\"}]",
                "[{\"text\":\"Line one\\nLine two\"}]",
                "[null]");
        for (String followUps : invalid) {
            assertThrows(IllegalValueException.class, () -> load(withFollowUps(followUps)), followUps);
        }
    }

    @Test
    public void nonStringFollowUpText_rejectedBeforeCoercion() {
        assertThrows(Exception.class, () -> load(withFollowUps("[{\"text\":12}]")));
    }
}
