package seedu.address.model.person;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/** Matches students by optional schooling level and at least one qualifying lesson. */
public final class StudentLessonMatchesPredicate implements Predicate<Student> {
    private final Optional<SchoolingLevelContainsKeywordPredicate> levelPredicate;
    private final LessonMatchesPredicate lessonPredicate;

    /** Creates a student predicate from the supplied level and lesson criteria. */
    public StudentLessonMatchesPredicate(Optional<String> level, LessonMatchesPredicate lessonPredicate) {
        levelPredicate = Objects.requireNonNull(level).map(SchoolingLevelContainsKeywordPredicate::new);
        this.lessonPredicate = Objects.requireNonNull(lessonPredicate);
    }

    @Override
    public boolean test(Student student) {
        return levelPredicate.map(predicate -> predicate.test(student)).orElse(true)
                && student.getLessons().stream().anyMatch(lessonPredicate);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StudentLessonMatchesPredicate predicate
                && levelPredicate.equals(predicate.levelPredicate)
                && lessonPredicate.equals(predicate.lessonPredicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(levelPredicate, lessonPredicate);
    }
}
