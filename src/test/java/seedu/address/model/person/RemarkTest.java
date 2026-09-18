package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {
    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void value_emptyOrFreeText_preserved() {
        assertEquals("", new Remark("").value);
        assertEquals("Likes 日本語!", new Remark("Likes 日本語!").toString());
    }

    @Test
    public void equalsAndHashCode() {
        Remark remark = new Remark("Likes swimming");
        assertEquals(remark, remark);
        assertEquals(remark, new Remark("Likes swimming"));
        assertEquals(remark.hashCode(), new Remark("Likes swimming").hashCode());
        assertNotEquals(remark, new Remark(""));
        assertNotEquals(remark, null);
        assertNotEquals(remark, "Likes swimming");
    }
}
