package seedu.address.testutil;

import java.util.Optional;

import seedu.address.logic.commands.LessonEditCommand.EditLessonDescriptor;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;

/**
 * Builds lesson edits, including explicitly cleared optional fields.
 */
public class EditLessonDescriptorBuilder {
    private final EditLessonDescriptor descriptor = new EditLessonDescriptor();

    /**
     * Sets the subject to edit.
     */
    public EditLessonDescriptorBuilder withSubject(Subject subject) {
        descriptor.setSubject(subject);
        return this;
    }

    /**
     * Sets the weekly slot to edit.
     */
    public EditLessonDescriptorBuilder withLessonSlot(WeeklyLessonSlot slot) {
        descriptor.setLessonSlot(slot);
        return this;
    }

    /**
     * Sets the hourly rate to edit.
     */
    public EditLessonDescriptorBuilder withHourlyRate(HourlyRate rate) {
        descriptor.setHourlyRate(rate);
        return this;
    }

    /**
     * Sets or clears the lesson location.
     */
    public EditLessonDescriptorBuilder withLessonLocation(Optional<LessonLocation> location) {
        descriptor.setLessonLocation(location);
        return this;
    }

    /**
     * Sets or clears the lesson mode.
     */
    public EditLessonDescriptorBuilder withLessonMode(Optional<LessonMode> mode) {
        descriptor.setLessonMode(mode);
        return this;
    }

    public EditLessonDescriptor build() {
        return descriptor;
    }
}
