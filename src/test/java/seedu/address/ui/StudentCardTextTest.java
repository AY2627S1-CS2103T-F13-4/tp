package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.GuardianContact;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.PersonBuilder;

public class StudentCardTextTest {

    private static final Person EMPTY = new PersonBuilder().build();

    // A missing detail must come back empty so the card shows a muted placeholder rather than blank space.
    @Test
    public void unrecordedDetails_areEmpty() {
        assertEquals(Optional.empty(), StudentCardText.subjects(EMPTY));
        assertEquals(Optional.empty(), StudentCardText.lesson(EMPTY));
        assertEquals(Optional.empty(), StudentCardText.rate(EMPTY));
        assertEquals(Optional.empty(), StudentCardText.guardian(EMPTY));
    }

    // Tutors list subjects in their own priority order, so the card must not reorder them.
    @Test
    public void subjects_keepEnteredOrder() {
        Person person = new PersonBuilder()
                .withSubjects(new Subject("Science"), new Subject("Math"), new Subject("English")).build();
        assertEquals(Optional.of("Science, Math, English"), StudentCardText.subjects(person));
    }

    @Test
    public void rate_showsCurrencyAndUnit() {
        Person person = new PersonBuilder().withHourlyRate(new HourlyRate(new BigDecimal("45.5"))).build();
        assertEquals(Optional.of("SGD 45.50/hour"), StudentCardText.rate(person));
    }

    @Test
    public void lesson_showsDayAndTimes() {
        Person person = new PersonBuilder().withWeeklyLessonSlot(new WeeklyLessonSlot(DayOfWeek.TUESDAY,
                LocalTime.of(16, 0), LocalTime.of(17, 30))).build();
        assertEquals(Optional.of("Tuesday 4.00pm-5.30pm"), StudentCardText.lesson(person));
    }

    @Test
    public void guardian_showsNameAndPhone() {
        Person person = new PersonBuilder()
                .withGuardianContact(new GuardianContact(new Name("Janet Tan"), new Phone("91234567"))).build();
        assertEquals(Optional.of("Janet Tan (91234567)"), StudentCardText.guardian(person));
    }
}
