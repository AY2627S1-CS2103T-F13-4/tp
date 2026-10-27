package seedu.address.model.person;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;

/** How students are ordered by hourly rate within their school stage. */
public enum RateSortOrder {
    /** No rate ordering: students stay in the order they were added. */
    NONE,
    /** Lowest rate first, comparing each student's lowest lesson rate. */
    ASCENDING,
    /** Highest rate first, comparing each student's highest lesson rate. */
    DESCENDING;

    /**
     * Returns a comparator for this order. Students without lessons come last in both directions, and
     * students with equal rates compare as equal so the caller's tie-break decides.
     */
    public Comparator<Student> comparator() {
        if (this == NONE) {
            return (a, b) -> 0;
        }
        Comparator<BigDecimal> byRate = this == ASCENDING ? Comparator.naturalOrder() : Comparator.reverseOrder();
        return Comparator.comparing(student -> rateOf(student).map(rate -> rate.value).orElse(null),
                Comparator.nullsLast(byRate));
    }

    /**
     * Returns the lesson rate that represents the student in this order: their lowest for {@code ASCENDING} and
     * their highest for {@code DESCENDING}. Empty for {@code NONE} or a student without lessons.
     */
    public Optional<HourlyRate> rateOf(Student student) {
        if (this == NONE) {
            return Optional.empty();
        }
        Comparator<HourlyRate> byValue = Comparator.comparing(rate -> rate.value);
        return student.getLessons().stream().map(Lesson::getHourlyRate)
                .min(this == ASCENDING ? byValue : byValue.reversed());
    }
}
