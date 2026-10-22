package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class SchoolContainsKeywordPredicateTest {
    @Test
    public void toString_includesNormalizedSchoolAndAdditionalCriteria() {
        SchoolingLevelContainsKeywordPredicate level = new SchoolingLevelContainsKeywordPredicate("Secondary");
        SchoolContainsKeywordPredicate predicate = new SchoolContainsKeywordPredicate("RAFFLES", level);
        String expected = SchoolContainsKeywordPredicate.class.getCanonicalName()
                + "{keyword=raffles, additionalCriteria=" + level + "}";

        assertEquals(expected, predicate.toString());
    }

    @Test
    public void test_matchesSchoolSubstringIgnoringCaseAndExcludesAbsentSchools() {
        Student student = new PersonBuilder().withSchool(new School("Raffles Institution")).build();
        assertTrue(new SchoolContainsKeywordPredicate("fFlEs Ins").test(student));
        assertFalse(new SchoolContainsKeywordPredicate("Dunman").test(student));
        assertFalse(new SchoolContainsKeywordPredicate("Raffles").test(new PersonBuilder().build()));
    }

    @Test
    public void test_requiresAdditionalCriteriaWithoutRequiringLessons() {
        Student student = new PersonBuilder().withSchool(new School("Raffles Institution"))
                .withSchoolingLevel(new SchoolingLevel("Secondary 3")).build();
        assertTrue(new SchoolContainsKeywordPredicate("raffles",
                new SchoolingLevelContainsKeywordPredicate("secondary")).test(student));
        assertFalse(new SchoolContainsKeywordPredicate("raffles",
                new SchoolingLevelContainsKeywordPredicate("primary")).test(student));
        assertFalse(new SchoolContainsKeywordPredicate("dunman",
                new SchoolingLevelContainsKeywordPredicate("secondary")).test(student));
    }

    @Test
    public void equals_comparesSchoolIgnoringCaseAndAdditionalCriteria() {
        SchoolContainsKeywordPredicate predicate = new SchoolContainsKeywordPredicate("Raffles",
                new SchoolingLevelContainsKeywordPredicate("Secondary"));
        SchoolContainsKeywordPredicate equivalent = new SchoolContainsKeywordPredicate("RAFFLES",
                new SchoolingLevelContainsKeywordPredicate("SECONDARY"));
        assertEquals(predicate, predicate);
        assertEquals(predicate, equivalent);
        assertEquals(predicate.hashCode(), equivalent.hashCode());
        assertNotEquals(predicate, null);
        assertNotEquals(predicate, "Raffles");
        assertNotEquals(predicate, new SchoolContainsKeywordPredicate("Dunman",
                new SchoolingLevelContainsKeywordPredicate("Secondary")));
        assertNotEquals(predicate, new SchoolContainsKeywordPredicate("Raffles",
                new SchoolingLevelContainsKeywordPredicate("Primary")));
    }
}
