package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditPersonDescriptorTest {

    private static final WeeklyLessonSlot MONDAY_SLOT = new WeeklyLessonSlot(DayOfWeek.MONDAY,
            LocalTime.of(16, 0), LocalTime.of(17, 30));
    private static final WeeklyLessonSlot TUESDAY_SLOT = new WeeklyLessonSlot(DayOfWeek.TUESDAY,
            LocalTime.of(10, 0), LocalTime.of(11, 0));

    @Test
    public void isAnyFieldEdited_noFields_returnsFalse() {
        assertFalse(new EditPersonDescriptor().isAnyFieldEdited());
    }

    @Test
    public void isAnyFieldEdited_weeklyLessonSlotSet_returnsTrue() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withWeeklyLessonSlot(MONDAY_SLOT).build();
        assertTrue(descriptor.isAnyFieldEdited());
    }

    @Test
    public void isAnyFieldEdited_weeklyLessonSlotCleared_returnsTrue() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withWeeklyLessonSlot(null).build();
        assertTrue(descriptor.isAnyFieldEdited());
    }

    @Test
    public void equals() {
        // same values -> returns true
        EditPersonDescriptor descriptorWithSameValues = new EditPersonDescriptor(DESC_AMY);
        assertTrue(DESC_AMY.equals(descriptorWithSameValues));

        // same object -> returns true
        assertTrue(DESC_AMY.equals(DESC_AMY));

        // null -> returns false
        assertFalse(DESC_AMY.equals(null));

        // different types -> returns false
        assertFalse(DESC_AMY.equals(5));

        // different values -> returns false
        assertFalse(DESC_AMY.equals(DESC_BOB));

        // different name -> returns false
        EditPersonDescriptor editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withName(VALID_NAME_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different phone -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withPhone(VALID_PHONE_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different email -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different address -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different tags -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different schooling level -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withSchoolingLevel("Primary 5").build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // clearing the level differs from setting it
        EditPersonDescriptor cleared = new EditPersonDescriptorBuilder(DESC_AMY).withSchoolingLevel(null).build();
        assertFalse(editedAmy.equals(cleared));
        assertFalse(DESC_AMY.equals(cleared));
    }

    @Test
    public void equals_weeklyLessonSlotEdits_comparesValueAndPresence() {
        EditPersonDescriptor unchanged = new EditPersonDescriptorBuilder(DESC_AMY).build();
        EditPersonDescriptor set = new EditPersonDescriptorBuilder(DESC_AMY).withWeeklyLessonSlot(MONDAY_SLOT).build();
        EditPersonDescriptor different = new EditPersonDescriptorBuilder(DESC_AMY)
                .withWeeklyLessonSlot(TUESDAY_SLOT).build();
        EditPersonDescriptor cleared = new EditPersonDescriptorBuilder(DESC_AMY).withWeeklyLessonSlot(null).build();

        assertEquals(set, new EditPersonDescriptorBuilder(DESC_AMY).withWeeklyLessonSlot(
                new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(16, 0), LocalTime.of(17, 30))).build());
        assertEquals(cleared, new EditPersonDescriptorBuilder(DESC_AMY).withWeeklyLessonSlot(null).build());
        assertNotEquals(set, different);
        assertNotEquals(set, cleared);
        assertNotEquals(unchanged, set);
        assertNotEquals(unchanged, cleared);
    }

    @Test
    public void copyConstructor_weeklyLessonSlotEdits_preservesValueAndPresence() {
        EditPersonDescriptor unchanged = new EditPersonDescriptor();
        EditPersonDescriptor set = new EditPersonDescriptorBuilder().withWeeklyLessonSlot(MONDAY_SLOT).build();
        EditPersonDescriptor cleared = new EditPersonDescriptorBuilder().withWeeklyLessonSlot(null).build();

        assertEquals(unchanged, new EditPersonDescriptor(unchanged));
        assertEquals(set, new EditPersonDescriptor(set));
        assertEquals(cleared, new EditPersonDescriptor(cleared));
    }

    @Test
    public void toStringMethod() {
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        String expected = EditPersonDescriptor.class.getCanonicalName() + "{name="
                + editPersonDescriptor.getName().orElse(null) + ", phone="
                + editPersonDescriptor.getPhone().orElse(null) + ", email="
                + editPersonDescriptor.getEmail().orElse(null) + ", address="
                + editPersonDescriptor.getAddress().orElse(null) + ", tags="
                + editPersonDescriptor.getTags().orElse(null) + ", schoolingLevel="
                + editPersonDescriptor.getSchoolingLevel().orElse(null) + ", weeklyLessonSlot="
                + editPersonDescriptor.getWeeklyLessonSlot().orElse(null) + "}";
        assertEquals(expected, editPersonDescriptor.toString());
    }
}
