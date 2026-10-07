package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

/** One guardian's name and phone, stored together as an immutable value. */
public final class GuardianContact {
    private final Name name;
    private final Phone phone;

    /** Creates a contact with both required details. */
    public GuardianContact(Name name, Phone phone) {
        requireAllNonNull(name, phone);
        this.name = name;
        this.phone = phone;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GuardianContact contact && name.equals(contact.name) && phone.equals(contact.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, phone);
    }

    @Override
    public String toString() {
        return name + ": " + phone;
    }
}
