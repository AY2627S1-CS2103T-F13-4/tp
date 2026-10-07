package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.math.BigDecimal;

/** An agreed SGD hourly rate. Absence is represented outside this value object. */
public final class HourlyRate {
    public static final String MESSAGE_CONSTRAINTS =
            "Hourly rate must be a non-negative SGD amount with at most two decimal places.";
    public final BigDecimal value;

    /** Creates a rate with a canonical two-decimal representation. */
    public HourlyRate(BigDecimal value) {
        requireNonNull(value);
        checkArgument(value.signum() >= 0 && value.scale() <= 2, MESSAGE_CONSTRAINTS);
        this.value = value.setScale(2);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof HourlyRate rate && value.equals(rate.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}
