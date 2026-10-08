package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code SchoolingLevel} contains the given text, ignoring case.
 * Persons without a schooling level never match.
 */
public class SchoolingLevelContainsKeywordPredicate implements Predicate<Person> {
    private final String keyword;

    /** Creates a predicate matching levels that contain {@code keyword}, ignoring case. */
    public SchoolingLevelContainsKeywordPredicate(String keyword) {
        requireNonNull(keyword);
        this.keyword = keyword.toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean test(Person person) {
        return person.getSchoolingLevel()
                .map(level -> level.value.toLowerCase(Locale.ROOT).contains(keyword))
                .orElse(false);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof SchoolingLevelContainsKeywordPredicate otherPredicate)) {
            return false;
        }

        return keyword.equals(otherPredicate.keyword);
    }

    @Override
    public int hashCode() {
        return keyword.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keyword", keyword).toString();
    }
}
