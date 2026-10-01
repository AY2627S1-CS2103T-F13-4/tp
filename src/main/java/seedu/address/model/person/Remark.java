package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents a remark about a person in the address book.
 */
public class Remark {

    public final String value;

    /**
     * Creates a {@code Remark} containing the given text.
     */
    public Remark(String remark) {
        requireNonNull(remark);
        value = remark;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof Remark otherRemark
                && value.equals(otherRemark.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
