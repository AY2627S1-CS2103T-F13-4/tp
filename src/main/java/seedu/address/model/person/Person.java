package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

/**
 * Represents any person tracked by the app, such as a student or a guardian.
 * Holds the identity details common to every kind of person.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public abstract class Person {

    private final Name name;
    private final Phone phone;

    /** Every field must be present and not null. */
    protected Person(Name name, Phone phone) {
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

    /** Returns true if {@code other} is the same kind of person with the same name. */
    public boolean isSamePerson(Person other) {
        if (other == this) {
            return true;
        }

        return other != null
                && other.getClass().equals(getClass())
                && other.getName().equals(getName());
    }

    /** Returns true if both persons are of the same kind and have the same name and phone. */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        return other instanceof Person otherPerson
                && other.getClass().equals(getClass())
                && name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, phone);
    }
}
