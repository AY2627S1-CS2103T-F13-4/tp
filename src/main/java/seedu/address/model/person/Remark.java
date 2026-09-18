package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * An optional note about a person. An empty value means no remark.
 */
public class Remark {
    public final String value;

    /**
     * Creates a remark with the given text, which may be empty.
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
        return other == this || (other instanceof Remark otherRemark && value.equals(otherRemark.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
