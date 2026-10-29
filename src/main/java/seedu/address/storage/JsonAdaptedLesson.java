package seedu.address.storage;

import java.math.BigDecimal;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.Subject;

/** Jackson-friendly version of {@link Lesson}. */
class JsonAdaptedLesson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Lesson's %s field is missing!";

    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String subject;
    private final JsonAdaptedWeeklyLessonSlot weeklyLessonSlot;
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String hourlyRate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String location;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String mode;

    /** Constructs a {@code JsonAdaptedLesson} with the given lesson details. */
    @JsonCreator
    JsonAdaptedLesson(@JsonProperty("subject") String subject,
            @JsonProperty("weeklyLessonSlot") JsonAdaptedWeeklyLessonSlot weeklyLessonSlot,
            @JsonProperty("hourlyRate") String hourlyRate, @JsonProperty("location") String location,
            @JsonProperty("mode") String mode) {
        this.subject = subject;
        this.weeklyLessonSlot = weeklyLessonSlot;
        this.hourlyRate = hourlyRate;
        this.location = location;
        this.mode = mode;
    }

    /** Converts a given {@code Lesson} into this class for Jackson use. */
    JsonAdaptedLesson(Lesson source) {
        subject = source.getSubject().toString();
        weeklyLessonSlot = new JsonAdaptedWeeklyLessonSlot(source.getLessonSlot());
        hourlyRate = source.getHourlyRate().toString();
        location = source.getLocation().map(LessonLocation::toString).orElse(null);
        mode = source.getMode().map(LessonMode::name).orElse(null);
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
        if (!HourlyRate.isValidHourlyRate(hourlyRate)) {
            throw new IllegalValueException(HourlyRate.MESSAGE_CONSTRAINTS);
        }
        if (location != null && !LessonLocation.isValidLocation(location)) {
            throw new IllegalValueException(LessonLocation.MESSAGE_CONSTRAINTS);
        }
        if (mode != null && !LessonMode.isValidMode(mode)) {
            throw new IllegalValueException(LessonMode.MESSAGE_CONSTRAINTS);
        }
        return new Lesson(new Subject(subject), weeklyLessonSlot.toModelType(),
                new HourlyRate(new BigDecimal(hourlyRate)),
                Optional.ofNullable(location).map(LessonLocation::new),
                Optional.ofNullable(mode).map(LessonMode::fromString));
    }
}
