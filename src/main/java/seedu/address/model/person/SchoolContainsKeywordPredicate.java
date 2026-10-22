package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;

/** Matches a school substring, ignoring case, together with any other student search criteria. */
public final class SchoolContainsKeywordPredicate implements Predicate<Student> {
    private final String keyword;
    private final Predicate<Student> additionalCriteria;

    /** Creates a school search that does not require students to have lessons. */
    public SchoolContainsKeywordPredicate(String keyword) {
        this(keyword, Model.PREDICATE_SHOW_ALL_PERSONS);
    }

    /** Creates a school search requiring the other supplied student criteria to match too. */
    public SchoolContainsKeywordPredicate(String keyword, Predicate<Student> additionalCriteria) {
        this.keyword = requireNonNull(keyword).toLowerCase(Locale.ROOT);
        this.additionalCriteria = requireNonNull(additionalCriteria);
    }

    @Override
    public boolean test(Student student) {
        return student.getSchool().map(school -> school.value.toLowerCase(Locale.ROOT).contains(keyword))
                .orElse(false) && additionalCriteria.test(student);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SchoolContainsKeywordPredicate predicate
                && keyword.equals(predicate.keyword)
                && additionalCriteria.equals(predicate.additionalCriteria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyword, additionalCriteria);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("keyword", keyword)
                .add("additionalCriteria", additionalCriteria)
                .toString();
    }
}
