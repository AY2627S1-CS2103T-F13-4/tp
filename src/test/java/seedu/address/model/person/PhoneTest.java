package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone("")); // empty string
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("911")); // fewer than 7 digits
        assertFalse(Phone.isValidPhone("123456")); // fewer than 7 digits
        assertFalse(Phone.isValidPhone("1234567890123456")); // more than 15 digits
        assertFalse(Phone.isValidPhone("phone")); // non-numeric
        assertFalse(Phone.isValidPhone("9011p041")); // alphabets within digits
        assertFalse(Phone.isValidPhone("9312  1534")); // repeated spaces within digits
        assertFalse(Phone.isValidPhone(" 93121534")); // leading space
        assertFalse(Phone.isValidPhone("9312-")); // trailing separator
        assertFalse(Phone.isValidPhone("65+93121534")); // + not at the start
        assertFalse(Phone.isValidPhone("+ 6593121534")); // space after +

        // valid phone numbers
        assertTrue(Phone.isValidPhone("9312153")); // exactly 7 digits
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("9312 1534")); // space within digits
        assertTrue(Phone.isValidPhone("9312-1534")); // hyphen within digits
        assertTrue(Phone.isValidPhone("+65 9312 1534")); // country code
        assertTrue(Phone.isValidPhone("124293842033123")); // exactly 15 digits
    }

    @Test
    public void constructor_separators_removed() {
        assertEquals("+6593121534", new Phone("+65 9312 1534").value);
        assertEquals("93121534", new Phone("9312-1534").value);
        assertEquals(new Phone("93121534"), new Phone("9312 1534"));
    }

    @Test
    public void equals() {
        Phone phone = new Phone("99999999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("99999999")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("99999995")));
    }
}
