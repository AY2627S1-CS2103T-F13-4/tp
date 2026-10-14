package seedu.address.model.person;

import java.time.DayOfWeek;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/** Matches a lesson's subject and day when those search criteria are supplied. */
public final class LessonMatchesPredicate implements Predicate<Lesson> {
    private final Optional<String> subject;
    private final Optional<DayOfWeek> day;

    /** Creates a predicate from optional subject text and weekday. */
    public LessonMatchesPredicate(Optional<String> subject, Optional<DayOfWeek> day) {
        this.subject = Objects.requireNonNull(subject).map(value -> value.toLowerCase(Locale.ROOT));
        this.day = Objects.requireNonNull(day);
    }

    @Override
    public boolean test(Lesson lesson) {
        return subject.map(value -> lesson.getSubject().toString().toLowerCase(Locale.ROOT).contains(value))
                .orElse(true) && day.map(value -> lesson.getDay() == value).orElse(true);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LessonMatchesPredicate predicate
                && subject.equals(predicate.subject) && day.equals(predicate.day);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, day);
    }
}
