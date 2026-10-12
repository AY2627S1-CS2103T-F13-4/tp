package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    private static final Student PERSON = new PersonBuilder().withName("Alice Tan").withPhone("91234567")
            .withEmail("alice@example.com").withAddress("Clementi").withTags("student")
            .withSchoolingLevel(new SchoolingLevel("Primary 5")).build();
    private static final Lesson MATH_LESSON = new Lesson(new Subject("Math"),
            new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(16, 0), LocalTime.of(17, 30)),
            new HourlyRate(new BigDecimal("45.5")));
    private static final Lesson ENGLISH_LESSON = new Lesson(new Subject("English"),
            new WeeklyLessonSlot(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(11, 0)),
            new HourlyRate(new BigDecimal("40")));
    private static final String PERSON_DETAILS = "Alice Tan; Phone: 91234567; Email: alice@example.com; "
            + "Address: Clementi; Tags: [student]; Level: Primary 5";

    @Test
    public void format_lessonsPresent_includesEveryLessonInOrder() {
        Student person = new PersonBuilder(PERSON).withLessons(MATH_LESSON, ENGLISH_LESSON).build();

        assertEquals(PERSON_DETAILS + "; Lessons: " + MATH_LESSON + " | " + ENGLISH_LESSON,
                Messages.format(person));
    }

    @Test
    public void format_noLessons_omitsLessons() {
        assertEquals(PERSON_DETAILS, Messages.format(PERSON));
    }

    @Test
    public void format_lessonsCleared_omitsLessons() {
        Student person = new PersonBuilder(PERSON).withLessons(MATH_LESSON).build();
        Student cleared = new PersonBuilder(person).withLessons().build();

        assertEquals(PERSON_DETAILS, Messages.format(cleared));
    }
}
