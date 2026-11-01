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
        assertEquals(new SchoolingLevelContainsKeywordPredicate("Primary 5"),
                new SchoolingLevelContainsKeywordPredicate(" Primary \t 5 "));
        assertNotEquals(predicate, new SchoolingLevelContainsKeywordPredicate("Secondary"));
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, 1);
    }

    @Test
    public void test() {
        Student primary = new PersonBuilder().withSchoolingLevel(new SchoolingLevel("Primary 5")).build();

        assertTrue(new SchoolingLevelContainsKeywordPredicate("Primary 5").test(primary));
        assertTrue(new SchoolingLevelContainsKeywordPredicate("pRiMaRy").test(primary));
        assertTrue(new SchoolingLevelContainsKeywordPredicate("5").test(primary));
        assertFalse(new SchoolingLevelContainsKeywordPredicate("Secondary").test(primary));

        // stage words and their starts match the other forms of a stage
        Student p5 = new PersonBuilder().withSchoolingLevel(new SchoolingLevel("P5")).build();
        Student sec3 = new PersonBuilder().withSchoolingLevel(new SchoolingLevel("Sec 3 Express")).build();
        Student jc1 = new PersonBuilder().withSchoolingLevel(new SchoolingLevel("JC1")).build();
        assertTrue(new SchoolingLevelContainsKeywordPredicate("prim").test(p5));
        assertTrue(new SchoolingLevelContainsKeywordPredicate("Primary 5").test(p5));
        assertTrue(new SchoolingLevelContainsKeywordPredicate("Secondary").test(sec3));
        assertTrue(new SchoolingLevelContainsKeywordPredicate("Secondary 3 express").test(sec3));
        assertTrue(new SchoolingLevelContainsKeywordPredicate("junior college 1").test(jc1));
        assertFalse(new SchoolingLevelContainsKeywordPredicate("Primary 6").test(p5));
        assertFalse(new SchoolingLevelContainsKeywordPredicate("Secondary 4").test(sec3));
        assertFalse(new SchoolingLevelContainsKeywordPredicate("prim").test(sec3));
        assertFalse(new SchoolingLevelContainsKeywordPredicate("Poly").test(p5));

        // the year after a stage word must start the rest of the level, not appear anywhere in it
        Student sec2Banded = new PersonBuilder().withSchoolingLevel(new SchoolingLevel("Sec 2 G3")).build();
        assertTrue(new SchoolingLevelContainsKeywordPredicate("Secondary 2").test(sec2Banded));
        assertFalse(new SchoolingLevelContainsKeywordPredicate("Secondary 3").test(sec2Banded));

        // extra spaces in the query count as one, like those in stored levels
        assertTrue(new SchoolingLevelContainsKeywordPredicate("Sec 3   Express").test(sec3));
        assertTrue(new SchoolingLevelContainsKeywordPredicate("Secondary  3 \t express").test(sec3));
        assertTrue(new SchoolingLevelContainsKeywordPredicate("Primary   5").test(primary));

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
