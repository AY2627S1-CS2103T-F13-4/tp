package seedu.address.testutil;

import seedu.address.logic.commands.GuardianEditCommand.EditGuardianDescriptor;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

/**
 * Builds guardian edits for command and parser tests.
 */
public class EditGuardianDescriptorBuilder {
    private final EditGuardianDescriptor descriptor = new EditGuardianDescriptor();

    /**
     * Sets the guardian name to edit.
     */
    public EditGuardianDescriptorBuilder withName(Name name) {
        descriptor.setName(name);
        return this;
    }

    /**
     * Sets the guardian phone to edit.
     */
    public EditGuardianDescriptorBuilder withPhone(Phone phone) {
        descriptor.setPhone(phone);
        return this;
    }

    public EditGuardianDescriptor build() {
        return descriptor;
    }
}
