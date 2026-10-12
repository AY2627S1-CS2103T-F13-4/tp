package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.DayOfWeek;
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
import seedu.address.ui.StudentCardText.Stage;

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
        assertEquals(Optional.empty(), StudentCardText.guardian(student));
    }

    // lesson-edit and lesson-delete take LESSON_INDEX, so the card must number lessons from 1 in stored order.
    // Location and mode are optional and stay empty when not recorded.
    @Test
    public void lessons_numberedInStoredOrderWithOptionalDetails() {
        Student student = new PersonBuilder().withLessons(MATH, ENGLISH).build();
        assertEquals(List.of(
                new LessonLine(1, "Math", "Tuesday 4.00pm-5.30pm", "SGD 60.00/hour", Optional.empty(),
                        Optional.empty()),
                new LessonLine(2, "English", "Saturday 10.00am-11.00am", "SGD 45.50/hour",
                        Optional.of("Tampines Library"), Optional.of(LessonMode.PHYSICAL))),
                StudentCardText.lessons(student));
    }

    // Common ways tutors write levels get their school stage's colour.
    @Test
    public void stageOf_recognisedForms() {
        for (String level : new String[] {"P5", "p 6", "Pri 3", "Primary 5", "primary 1 GEP"}) {
            assertEquals(Stage.PRIMARY, StudentCardText.stageOf(level), level);
        }
        for (String level : new String[] {"Sec 3", "S4", "Secondary 2", "sec 4 Express", "Sec 1 NA"}) {
            assertEquals(Stage.SECONDARY, StudentCardText.stageOf(level), level);
        }
        for (String level : new String[] {"JC1", "jc 2", "J2", "Junior College 1"}) {
            assertEquals(Stage.JC, StudentCardText.stageOf(level), level);
        }
    }

    // The level is free text, so anything not clearly a stage must stay neutral rather than risk a wrong colour.
    @Test
    public void stageOf_unclearForms_areOther() {
        for (String level : new String[] {"Poly Y1", "IB Year 1", "P7", "Sec 6", "JC4", "Kindergarten", "Pre-U"}) {
            assertEquals(Stage.OTHER, StudentCardText.stageOf(level), level);
        }
    }

    @Test
    public void guardian_showsNameAndPhone() {
        Student student = new PersonBuilder()
                .withGuardian(new Guardian(new Name("Janet Tan"), new Phone("91234567"))).build();
        assertEquals(Optional.of("Janet Tan (91234567)"), StudentCardText.guardian(student));
    }
}
