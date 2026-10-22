package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class StudentContactMatchesPredicateTest {

    @Test
    public void equals_comparesEverySearchCriterionIgnoringContactCase() {
        StudentContactMatchesPredicate predicate = new StudentContactMatchesPredicate(
                Optional.of("Alex"), Optional.of("Janet"),
                new SchoolingLevelContainsKeywordPredicate("Secondary"));
        StudentContactMatchesPredicate equivalent = new StudentContactMatchesPredicate(
                Optional.of("ALEX"), Optional.of("JANET"),
                new SchoolingLevelContainsKeywordPredicate("secondary"));

        assertEquals(predicate, predicate);
        assertEquals(predicate, equivalent);
        assertEquals(equivalent, predicate);
        assertEquals(predicate.hashCode(), equivalent.hashCode());
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, "Alex");
        assertNotEquals(predicate, new StudentContactMatchesPredicate(
                Optional.of("Bob"), Optional.of("Janet"),
                new SchoolingLevelContainsKeywordPredicate("Secondary")));
        assertNotEquals(predicate, new StudentContactMatchesPredicate(
                Optional.of("Alex"), Optional.of("John"),
                new SchoolingLevelContainsKeywordPredicate("Secondary")));
        assertNotEquals(predicate, new StudentContactMatchesPredicate(
                Optional.of("Alex"), Optional.of("Janet"),
                new SchoolingLevelContainsKeywordPredicate("Primary")));
        assertNotEquals(predicate, new StudentContactMatchesPredicate(
                Optional.empty(), Optional.of("Janet"),
                new SchoolingLevelContainsKeywordPredicate("Secondary")));
        assertNotEquals(predicate, new StudentContactMatchesPredicate(
                Optional.of("Alex"), Optional.empty(),
                new SchoolingLevelContainsKeywordPredicate("Secondary")));
    }
}
