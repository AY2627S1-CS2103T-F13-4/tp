package seedu.address.model.person;

/** One guardian's name and phone, stored together as an immutable value. */
public final class Guardian extends Person {

    /** Creates a contact with both required details. */
    public Guardian(Name name, Phone phone) {
        super(name, phone);
    }

    @Override
    public String toString() {
        return getName() + ": " + getPhone();
    }
}
