package seedu.address.storage;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Subject;

/** Jackson-friendly version of {@link Lesson}. */
class JsonAdaptedLesson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Lesson's %s field is missing!";

    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String subject;
    private final JsonAdaptedWeeklyLessonSlot weeklyLessonSlot;
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String hourlyRate;

    /** Constructs a {@code JsonAdaptedLesson} with the given lesson details. */
    @JsonCreator
    JsonAdaptedLesson(@JsonProperty("subject") String subject,
            @JsonProperty("weeklyLessonSlot") JsonAdaptedWeeklyLessonSlot weeklyLessonSlot,
            @JsonProperty("hourlyRate") String hourlyRate) {
        this.subject = subject;
        this.weeklyLessonSlot = weeklyLessonSlot;
        this.hourlyRate = hourlyRate;
    }

    /** Converts a given {@code Lesson} into this class for Jackson use. */
    JsonAdaptedLesson(Lesson source) {
        subject = source.getSubject().toString();
        weeklyLessonSlot = new JsonAdaptedWeeklyLessonSlot(source.getWeeklyLessonSlot());
        hourlyRate = source.getHourlyRate().toString();
    }

    /**
     * Converts this Jackson-friendly adapted lesson into the model's {@code Lesson} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted lesson.
     */
    Lesson toModelType() throws IllegalValueException {
        if (subject == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "subject"));
        }
        if (!Subject.isValidSubject(subject)) {
            throw new IllegalValueException(Subject.MESSAGE_CONSTRAINTS);
        }
        if (weeklyLessonSlot == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "weeklyLessonSlot"));
        }
        if (hourlyRate == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "hourlyRate"));
        }
        if (!hourlyRate.matches("[0-9]+(\\.[0-9]{1,2})?")) {
            throw new IllegalValueException(HourlyRate.MESSAGE_CONSTRAINTS);
        }
        return new Lesson(new Subject(subject), weeklyLessonSlot.toModelType(),
                new HourlyRate(new BigDecimal(hourlyRate)));
    }
}
