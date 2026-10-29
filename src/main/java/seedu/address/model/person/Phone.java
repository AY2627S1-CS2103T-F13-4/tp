package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Student's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {


    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers should have 7 to 15 digits, optionally starting with + for the country code. "
            + "Groups of digits may be separated by single spaces or hyphens, e.g. +65 9123 4567.";
    public static final String VALIDATION_REGEX = "\\+?\\d+([ -]\\d+)*";
    private static final int MIN_DIGITS = 7;
    private static final int MAX_DIGITS = 15;
    public final String value;

    /**
     * Constructs a {@code Phone}, removing the spaces and hyphens between groups of digits.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = phone.replaceAll("[ -]", "");
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        if (!test.matches(VALIDATION_REGEX)) {
            return false;
        }
        long digitCount = test.chars().filter(c -> c >= '0' && c <= '9').count();
        return digitCount >= MIN_DIGITS && digitCount <= MAX_DIGITS;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
