package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class SchoolTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new School(null));
    }

    @Test
    public void constructor_invalidSchool_throwsIllegalArgumentException() {
        for (String school : new String[] {"", " \t\n", "\u2003", "A".repeat(61)}) {
            assertThrows(IllegalArgumentException.class, School.MESSAGE_CONSTRAINTS, () -> new School(school));
        }
    }

    @Test
    public void constructor_trimsOuterWhitespace_preservesInternalWhitespace() {
        School school = new School(" \tNanyang  Girls' High School \n");
        assertEquals("Nanyang  Girls' High School", school.value);
        assertEquals(school.value, school.toString());
    }

    @Test
    public void isValidSchool() {
        assertThrows(NullPointerException.class, () -> School.isValidSchool(null));
        assertFalse(School.isValidSchool(""));
        assertFalse(School.isValidSchool(" \t\n"));
        assertFalse(School.isValidSchool("\u2003"));
        assertFalse(School.isValidSchool("A".repeat(61)));

        assertTrue(School.isValidSchool("A"));
        assertTrue(School.isValidSchool("A".repeat(60)));
        assertTrue(School.isValidSchool(" " + "A".repeat(60) + " "));
        assertTrue(School.isValidSchool("St. Joseph's Institution (Junior)"));
        assertTrue(School.isValidSchool("School #1 & 2 - North/East"));
        assertTrue(School.isValidSchool("\u5357\u534e\u4e2d\u5b66"));
    }

    @Test
    public void equals() {
        School school = new School("Nanyang Primary School");
        School sameSchool = new School(" Nanyang Primary School ");
        assertTrue(school.equals(school));
        assertTrue(school.equals(sameSchool));
        assertEquals(school.hashCode(), sameSchool.hashCode());
        assertFalse(school.equals(null));
        assertFalse(school.equals("Nanyang Primary School"));
        assertFalse(school.equals(new School("Nanyang Girls' High School")));
    }
}
