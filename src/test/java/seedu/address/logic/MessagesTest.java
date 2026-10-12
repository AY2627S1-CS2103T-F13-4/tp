package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    private static final Student PERSON = new PersonBuilder().withName("Alice Tan").withPhone("91234567")
            .withEmail("alice@example.com").withAddress("Clementi").withTags("student")
            .withSchoolingLevel(new SchoolingLevel("Primary 5")).build();
    private static final WeeklyLessonSlot LESSON_SLOT = new WeeklyLessonSlot(DayOfWeek.MONDAY,
            LocalTime.of(16, 0), LocalTime.of(17, 30));
    private static final String PERSON_DETAILS = "Alice Tan; Phone: 91234567; Email: alice@example.com; "
            + "Address: Clementi; Tags: [student]; Level: Primary 5";

    @Test
    public void format_weeklyLessonSlotPresent_includesSlot() {
        Student person = new PersonBuilder(PERSON).withWeeklyLessonSlot(LESSON_SLOT).build();

        assertEquals(PERSON_DETAILS + "; Lesson Slot: " + LESSON_SLOT, Messages.format(person));
    }

    @Test
    public void format_weeklyLessonSlotAbsent_omitsSlot() {
        assertEquals(PERSON_DETAILS, Messages.format(PERSON));
    }

    @Test
    public void format_weeklyLessonSlotCleared_omitsSlot() {
        Student person = new PersonBuilder(PERSON).withWeeklyLessonSlot(LESSON_SLOT).build();
        Student cleared = new PersonBuilder(person).withWeeklyLessonSlot(null).build();

        assertEquals(PERSON_DETAILS, Messages.format(cleared));
    }
}
