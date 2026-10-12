package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Guardian;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;

/** JSON representation of a complete guardian contact. */
class JsonAdaptedGuardian {
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String name;
    @JsonDeserialize(using = StrictStringDeserializer.class)
    private final String phone;

    @JsonCreator
    JsonAdaptedGuardian(@JsonProperty("name") String name, @JsonProperty("phone") String phone) {
        this.name = name;
        this.phone = phone;
    }

    JsonAdaptedGuardian(Guardian contact) {
        name = contact.getName().fullName;
        phone = contact.getPhone().value;
    }

    Guardian toModelType() throws IllegalValueException {
        if (name == null || !Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        if (phone == null || !Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Guardian(new Name(name), new Phone(phone));
    }
}
