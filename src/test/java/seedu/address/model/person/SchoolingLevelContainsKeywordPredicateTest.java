package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class SchoolingLevelContainsKeywordPredicateTest {

    @Test
    public void equals() {
        SchoolingLevelContainsKeywordPredicate predicate = new SchoolingLevelContainsKeywordPredicate("Primary");

        assertEquals(predicate, predicate);
        assertEquals(predicate, new SchoolingLevelContainsKeywordPredicate("primary"));
        assertEquals(predicate.hashCode(), new SchoolingLevelContainsKeywordPredicate("PRIMARY").hashCode());
        assertNotEquals(predicate, new SchoolingLevelContainsKeywordPredicate("Secondary"));
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, 1);
    }

    @Test
    public void test() {
        Person primary = new PersonBuilder().withSchoolingLevel(new SchoolingLevel("Primary 5")).build();

        assertTrue(new SchoolingLevelContainsKeywordPredicate("Primary 5").test(primary));
        assertTrue(new SchoolingLevelContainsKeywordPredicate("pRiMaRy").test(primary));
        assertTrue(new SchoolingLevelContainsKeywordPredicate("5").test(primary));
        assertFalse(new SchoolingLevelContainsKeywordPredicate("Secondary").test(primary));

        // no recorded level never matches
        assertFalse(new SchoolingLevelContainsKeywordPredicate("Primary").test(new PersonBuilder().build()));
    }

    @Test
    public void toStringMethod() {
        SchoolingLevelContainsKeywordPredicate predicate = new SchoolingLevelContainsKeywordPredicate("Primary");
        String expected = SchoolingLevelContainsKeywordPredicate.class.getCanonicalName() + "{keyword=primary}";
        assertEquals(expected, predicate.toString());
    }
}
