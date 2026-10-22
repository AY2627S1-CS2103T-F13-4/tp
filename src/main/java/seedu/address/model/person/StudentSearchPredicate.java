package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/** Requires every supplied student criterion and, when present, at least one matching lesson. */
public final class StudentSearchPredicate implements Predicate<Student> {
    private final Optional<String> name;
    private final Optional<String> guardian;
    private final Optional<String> school;
    private final Optional<SchoolingLevelContainsKeywordPredicate> level;
    private final Optional<LessonMatchesPredicate> lesson;

    /** Creates a search with optional case-insensitive contact, school, level and lesson criteria. */
    public StudentSearchPredicate(Optional<String> name, Optional<String> guardian, Optional<String> school,
            Optional<String> level, Optional<LessonMatchesPredicate> lesson) {
        this.name = normalize(name);
        this.guardian = normalize(guardian);
        this.school = normalize(school);
        this.level = requireNonNull(level).map(SchoolingLevelContainsKeywordPredicate::new);
        this.lesson = requireNonNull(lesson);
    }

    private static Optional<String> normalize(Optional<String> text) {
        return requireNonNull(text).map(value -> value.toLowerCase(Locale.ROOT));
    }

    private static boolean contains(String value, String text) {
        return value.toLowerCase(Locale.ROOT).contains(text);
    }

    @Override
    public boolean test(Student student) {
        requireNonNull(student);
        return name.map(text -> contains(student.getName().fullName, text)).orElse(true)
                && guardian.map(text -> student.getGuardian()
                        .map(contact -> contains(contact.getName().fullName, text)
                                || contains(contact.getPhone().value, text)).orElse(false)).orElse(true)
                && school.map(text -> student.getSchool().map(value -> contains(value.value, text))
                        .orElse(false)).orElse(true)
                && level.map(predicate -> predicate.test(student)).orElse(true)
                && lesson.map(predicate -> student.getLessons().stream().anyMatch(predicate)).orElse(true);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StudentSearchPredicate predicate
                && name.equals(predicate.name) && guardian.equals(predicate.guardian)
                && school.equals(predicate.school) && level.equals(predicate.level) && lesson.equals(predicate.lesson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, guardian, school, level, lesson);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("guardian", guardian)
                .add("school", school)
                .add("level", level)
                .add("lesson", lesson)
                .toString();
    }
}
