package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Student;
import seedu.address.model.tag.Tag;

public class SampleDataUtilTest {

    @Test
    public void getSampleAddressBook_containsAllSamplePersons() {
        ReadOnlyAddressBook addressBook = SampleDataUtil.getSampleAddressBook();
        Student[] samples = SampleDataUtil.getSamplePersons();

        assertEquals(samples.length, addressBook.getPersonList().size());
        for (Student sample : samples) {
            assertTrue(addressBook.getPersonList().contains(sample));
        }
    }

    @Test
    public void getTagSet_returnsTagsWithGivenNames() {
        assertEquals(2, SampleDataUtil.getTagSet("a", "b").size());
        assertTrue(SampleDataUtil.getTagSet("a", "b").containsAll(Arrays.asList(new Tag("a"), new Tag("b"))));
    }
}
