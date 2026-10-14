package seedu.address.ui.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.PersonBuilder;

public class ScheduleEntryTest {

    private static final Lesson LESSON = createLesson(DayOfWeek.MONDAY, "09:05", "10:47");
    private static final Student STUDENT = new PersonBuilder().withLessons(LESSON).build();

    @Test
    public void constructor_nullValues_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ScheduleEntry(null, LESSON));
        assertThrows(NullPointerException.class, () -> new ScheduleEntry(STUDENT, null));
        assertThrows(NullPointerException.class, () -> new ScheduleEntry(null, null));
    }

    @Test
    public void constructor_validValues_preservesStudentLessonAndTimes() {
        ScheduleEntry entry = new ScheduleEntry(STUDENT, LESSON);

        assertSame(STUDENT, entry.getStudent());
        assertSame(LESSON, entry.getLesson());
        assertEquals(LocalTime.of(9, 5), entry.getStart());
        assertEquals(LocalTime.of(10, 47), entry.getEnd());
    }

    @Test
    public void getDisplayColumn_newEntry_returnsZero() {
        ScheduleEntry entry = new ScheduleEntry(STUDENT, LESSON);

        assertEquals(0, entry.getDisplayColumn());
    }

    @Test
    public void setDisplayColumn_nonNegativeValues_updatesColumnAndPreservesEntry() {
        ScheduleEntry entry = new ScheduleEntry(STUDENT, LESSON);

        for (int column : List.of(0, 1, 4, Integer.MAX_VALUE, 0)) {
            entry.setDisplayColumn(column);
            assertEquals(column, entry.getDisplayColumn());
            assertSame(STUDENT, entry.getStudent());
            assertSame(LESSON, entry.getLesson());
            assertEquals(LocalTime.of(9, 5), entry.getStart());
            assertEquals(LocalTime.of(10, 47), entry.getEnd());
        }
    }

    @Test
    public void compareTo_differentDays_ordersByDayBeforeTime() {
        ScheduleEntry monday = createEntry(DayOfWeek.MONDAY, "23:00", "23:59");
        ScheduleEntry tuesday = createEntry(DayOfWeek.TUESDAY, "00:00", "00:01");
        ScheduleEntry sunday = createEntry(DayOfWeek.SUNDAY, "00:00", "00:01");

        assertTrue(monday.compareTo(tuesday) < 0);
        assertTrue(tuesday.compareTo(monday) > 0);
        assertTrue(tuesday.compareTo(sunday) < 0);
        assertTrue(sunday.compareTo(monday) > 0);
    }

    @Test
    public void compareTo_sameDayDifferentStarts_ordersByStartBeforeEnd() {
        ScheduleEntry earlier = createEntry(DayOfWeek.MONDAY, "09:00", "12:00");
        ScheduleEntry later = createEntry(DayOfWeek.MONDAY, "10:00", "11:00");

        assertTrue(earlier.compareTo(later) < 0);
        assertTrue(later.compareTo(earlier) > 0);
    }

    @Test
    public void compareTo_sameDayAndStartDifferentEnds_ordersByEnd() {
        ScheduleEntry shorter = createEntry(DayOfWeek.MONDAY, "09:00", "10:00");
        ScheduleEntry longer = createEntry(DayOfWeek.MONDAY, "09:00", "11:00");

        assertTrue(shorter.compareTo(longer) < 0);
        assertTrue(longer.compareTo(shorter) > 0);
    }

    @Test
    public void compareTo_sameSlotDifferentStudentsAndLessonDetails_returnsZero() {
        Lesson englishLesson = new Lesson(new Subject("English"), LESSON.getWeeklyLessonSlot(),
                new HourlyRate(new BigDecimal("60")));
        Student otherStudent = new PersonBuilder().withName("Bob Tan").withLessons(englishLesson).build();
        ScheduleEntry first = new ScheduleEntry(STUDENT, LESSON);
        ScheduleEntry second = new ScheduleEntry(otherStudent, englishLesson);

        assertEquals(0, first.compareTo(second));
        assertEquals(0, second.compareTo(first));
    }

    @Test
    public void compareTo_differentDisplayColumns_preservesSlotOrdering() {
        ScheduleEntry earlier = createEntry(DayOfWeek.MONDAY, "09:00", "10:00");
        ScheduleEntry sameSlot = createEntry(DayOfWeek.MONDAY, "09:00", "10:00");
        ScheduleEntry later = createEntry(DayOfWeek.MONDAY, "10:00", "11:00");
        earlier.setDisplayColumn(Integer.MAX_VALUE);

        assertEquals(0, earlier.compareTo(sameSlot));
        assertEquals(0, sameSlot.compareTo(earlier));
        assertTrue(earlier.compareTo(later) < 0);
        assertTrue(later.compareTo(earlier) > 0);
    }

    @Test
    public void compareTo_sameEntry_returnsZero() {
        ScheduleEntry entry = new ScheduleEntry(STUDENT, LESSON);

        assertEquals(0, entry.compareTo(entry));
    }

    @Test
    public void compareTo_null_throwsNullPointerException() {
        ScheduleEntry entry = new ScheduleEntry(STUDENT, LESSON);

        assertThrows(NullPointerException.class, () -> entry.compareTo(null));
    }

    private static ScheduleEntry createEntry(DayOfWeek day, String start, String end) {
        Lesson lesson = createLesson(day, start, end);
        Student student = new PersonBuilder().withLessons(lesson).build();
        return new ScheduleEntry(student, lesson);
    }

    private static Lesson createLesson(DayOfWeek day, String start, String end) {
        return new Lesson(new Subject("Math"),
                new WeeklyLessonSlot(day, LocalTime.parse(start), LocalTime.parse(end)),
                new HourlyRate(new BigDecimal("50")));
    }
}
