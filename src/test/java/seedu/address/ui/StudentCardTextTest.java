package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Guardian;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.LessonLocation;
import seedu.address.model.person.LessonMode;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.PersonBuilder;
import seedu.address.ui.StudentCardText.LessonLine;

public class StudentCardTextTest {

    private static final Lesson MATH = new Lesson(new Subject("Math"),
            new WeeklyLessonSlot(DayOfWeek.TUESDAY, LocalTime.of(16, 0), LocalTime.of(17, 30)),
            new HourlyRate(new BigDecimal("60")));
    private static final Lesson ENGLISH = new Lesson(new Subject("English"),
            new WeeklyLessonSlot(DayOfWeek.SATURDAY, LocalTime.of(10, 0), LocalTime.of(11, 0)),
            new HourlyRate(new BigDecimal("45.5")), Optional.of(new LessonLocation("Tampines Library")),
            Optional.of(LessonMode.PHYSICAL));

    // A student without lessons or a guardian must produce nothing, so the card shows a muted placeholder.
    @Test
    public void unrecordedDetails_areEmpty() {
        Student student = new PersonBuilder().build();
        assertEquals(List.of(), StudentCardText.lessons(student));
        assertEquals(Optional.empty(), StudentCardText.subjects(student));
        assertEquals(Optional.empty(), StudentCardText.guardian(student));
    }

    // lesson-edit and lesson-delete take LESSON_INDEX, so the card must number lessons from 1 in stored order.
    // Location and mode are optional and stay empty when not recorded.
    @Test
    public void lessons_numberedInStoredOrderWithOptionalDetails() {
        Student student = new PersonBuilder().withLessons(MATH, ENGLISH).build();
        assertEquals(List.of(
                new LessonLine(1, "Math", "Tue 4.00pm-5.30pm", "SGD 60/h", Optional.empty(), Optional.empty()),
                new LessonLine(2, "English", "Sat 10.00am-11.00am", "SGD 45.50/h",
                        Optional.of("Tampines Library"), Optional.of(LessonMode.PHYSICAL))),
                StudentCardText.lessons(student));
        assertEquals(Optional.of("Math, English"), StudentCardText.subjects(student));
        assertEquals("$45.50/h", StudentCardText.tableRate(StudentCardText.lessons(student).get(1)));
    }
    // The row shows the lesson that comes soonest in the week; one already under way counts from next week.
    @Test
    public void nextLesson_isSoonestUpcoming() {
        Student student = new PersonBuilder().withLessons(MATH, ENGLISH).build(); // Tue 4pm, Sat 10am
        List<LessonLine> lines = StudentCardText.lessons(student);
        List<WeeklyLessonSlot> slots = StudentCardText.slots(student);
        LocalDateTime wednesday = LocalDateTime.of(2026, 10, 14, 9, 0);
        LocalDateTime saturdayLater = LocalDateTime.of(2026, 10, 17, 10, 30);
        assertEquals("English", StudentCardText.nextLesson(lines, slots, wednesday).get().subject());
        assertEquals("Math", StudentCardText.nextLesson(lines, slots, saturdayLater).get().subject());
        assertEquals("Sat 10.00am", StudentCardText.dayAndStart(lines.get(1)));
        assertEquals(Optional.empty(), StudentCardText.nextLesson(List.of(), List.of(), wednesday));
    }

    @Test
    public void initials_takeFirstTwoWords() {
        assertEquals("AT", StudentCardText.initials("Alex Tan"));
        assertEquals("MH", StudentCardText.initials("muhammad hafiz bin abdul"));
        assertEquals("M", StudentCardText.initials("Madonna"));
    }

    @Test
    public void guardian_showsNameAndPhone() {
        Student student = new PersonBuilder()
                .withGuardian(new Guardian(new Name("Janet Tan"), new Phone("91234567"))).build();
        assertEquals(Optional.of("Janet Tan (91234567)"), StudentCardText.guardian(student));
    }
}
