package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.GuardianContact;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

/** JSON representation of a complete guardian contact. */
class JsonAdaptedGuardianContact {
    private final String name;
    private final String phone;

    @JsonCreator
    JsonAdaptedGuardianContact(@JsonProperty("name") String name, @JsonProperty("phone") String phone) {
        this.name = name;
        this.phone = phone;
    }

    JsonAdaptedGuardianContact(GuardianContact contact) {
        name = contact.getName().fullName;
        phone = contact.getPhone().value;
    }

    GuardianContact toModelType() throws IllegalValueException {
        if (name == null || !Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        if (phone == null || !Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new GuardianContact(new Name(name), new Phone(phone));
    }
}
