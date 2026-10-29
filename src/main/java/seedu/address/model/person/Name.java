package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Student's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names must start with a letter or number and contain only letters, accents, numbers, spaces, "
                    + "apostrophes, hyphens or periods. Relationship terms s/o and d/o are also allowed.";

    /*
     * Unicode letters and combining marks preserve names in different scripts and accent forms.
     * Requiring a letter or number first excludes blank and punctuation-only names.
     * A slash is accepted only within a whole s/o or d/o term.
     */
    public static final String VALIDATION_REGEX = "[\\p{L}\\p{N}]"
            + "(?:[\\p{L}\\p{M}\\p{N} .'’-]|(?<=\\b[sSdD])/(?=[oO](?: |$)))*";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        return test.matches(VALIDATION_REGEX);
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
