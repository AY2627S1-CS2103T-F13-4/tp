//@@author pranavp311
package seedu.address.storage;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.ProgressNote;

/** Jackson-friendly version of {@link ProgressNote}. */
class JsonAdaptedProgressNote {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Note's %s field is missing!";

    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String date;
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String text;

    /** Constructs a {@code JsonAdaptedProgressNote} with the given note details. */
    @JsonCreator
    JsonAdaptedProgressNote(@JsonProperty("date") String date, @JsonProperty("text") String text) {
        this.date = date;
        this.text = text;
    }

    /** Converts a given {@code ProgressNote} into this class for Jackson use. */
    JsonAdaptedProgressNote(ProgressNote source) {
        date = ProgressNote.formatDate(source.getDate());
        text = source.getText();
    }

    /**
     * Converts this Jackson-friendly adapted note into the model's {@code ProgressNote} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted note.
     */
    ProgressNote toModelType() throws IllegalValueException {
        if (date == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "date"));
        }
        if (text == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "text"));
        }
        if (!ProgressNote.isValidText(text)) {
            throw new IllegalValueException(ProgressNote.MESSAGE_TEXT_CONSTRAINTS);
        }
        LocalDate modelDate;
        try {
            modelDate = ProgressNote.parseDate(date);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(ProgressNote.MESSAGE_DATE_CONSTRAINTS);
        }
        return new ProgressNote(modelDate, text);
    }
}
