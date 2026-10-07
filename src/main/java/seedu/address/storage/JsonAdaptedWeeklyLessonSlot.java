package seedu.address.storage;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.WeeklyLessonSlot;

/** JSON representation of a weekly lesson using an uppercase day and HH:mm times. */
class JsonAdaptedWeeklyLessonSlot {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String day;
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String start;
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String end;

    @JsonCreator
    JsonAdaptedWeeklyLessonSlot(@JsonProperty("day") String day, @JsonProperty("start") String start,
            @JsonProperty("end") String end) {
        this.day = day;
        this.start = start;
        this.end = end;
    }

    JsonAdaptedWeeklyLessonSlot(WeeklyLessonSlot slot) {
        day = slot.getDay().name();
        start = slot.getStart().format(TIME_FORMAT);
        end = slot.getEnd().format(TIME_FORMAT);
    }

    WeeklyLessonSlot toModelType() throws IllegalValueException {
        if (day == null || start == null || end == null
                || !start.matches("[0-9]{2}:[0-9]{2}") || !end.matches("[0-9]{2}:[0-9]{2}")) {
            throw new IllegalValueException(WeeklyLessonSlot.MESSAGE_CONSTRAINTS);
        }
        try {
            return new WeeklyLessonSlot(DayOfWeek.valueOf(day), LocalTime.parse(start, TIME_FORMAT),
                    LocalTime.parse(end, TIME_FORMAT));
        } catch (DateTimeException | IllegalArgumentException e) {
            throw new IllegalValueException(WeeklyLessonSlot.MESSAGE_CONSTRAINTS);
        }
    }
}
