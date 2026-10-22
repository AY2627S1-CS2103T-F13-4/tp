package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.FollowUp;

/** Jackson-friendly version of {@link FollowUp}. */
class JsonAdaptedFollowUp {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Follow-up's %s field is missing!";

    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String text;

    /** Constructs a {@code JsonAdaptedFollowUp} with the given text. */
    @JsonCreator
    JsonAdaptedFollowUp(@JsonProperty("text") String text) {
        this.text = text;
    }

    /** Converts a given {@code FollowUp} into this class for Jackson use. */
    JsonAdaptedFollowUp(FollowUp source) {
        text = source.getText();
    }

    /**
     * Converts this Jackson-friendly adapted follow-up into the model's {@code FollowUp} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted follow-up.
     */
    FollowUp toModelType() throws IllegalValueException {
        if (text == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "text"));
        }
        if (!FollowUp.isValidText(text)) {
            throw new IllegalValueException(FollowUp.MESSAGE_TEXT_CONSTRAINTS);
        }
        return new FollowUp(text);
    }
}
