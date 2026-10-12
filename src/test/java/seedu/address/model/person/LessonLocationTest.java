package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LessonLocationTest {

    @Test
    public void constructor_invalidLocation_throwsIllegalArgumentException() {
        assertThrows(NullPointerException.class, () -> new LessonLocation(null));
        assertThrows(IllegalArgumentException.class, () -> new LessonLocation(""));
        assertThrows(IllegalArgumentException.class, () -> new LessonLocation("   "));
    }

    @Test
    public void isValidLocation() {
        assertThrows(NullPointerException.class, () -> LessonLocation.isValidLocation(null));

        assertFalse(LessonLocation.isValidLocation(""));
        assertFalse(LessonLocation.isValidLocation(" \t "));
        assertFalse(LessonLocation.isValidLocation("a".repeat(101)));
        assertFalse(LessonLocation.isValidLocation("room x/1"));
        assertFalse(LessonLocation.isValidLocation("x/1"));

        assertTrue(LessonLocation.isValidLocation("a"));
        assertTrue(LessonLocation.isValidLocation("a".repeat(100)));
        assertTrue(LessonLocation.isValidLocation("https://zoom.us/j/123"));
        assertTrue(LessonLocation.isValidLocation("Blk 123 #01-01"));
    }

    @Test
    public void constructor_normalizesWhitespace() {
        assertEquals("Blk 1 Library", new LessonLocation(" \tBlk  1\t Library  ").value);
        assertEquals("Library", new LessonLocation("Library").toString());
    }

    @Test
    public void equals() {
        LessonLocation location = new LessonLocation("Library");

        assertTrue(location.equals(location));
        assertTrue(location.equals(new LessonLocation(" library ")));
        assertEquals(location.hashCode(), new LessonLocation("LIBRARY").hashCode());

        assertFalse(location.equals(null));
        assertFalse(location.equals("Library"));
        assertNotEquals(location, new LessonLocation("Home"));
    }
}
