package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

/** Boundary tests for the shared tuition values. */
public class TuitionValuesTest {
    @Test
    public void labels_normalizeSpacingAndPreserveCase() {
        assertEquals(new Subject("Combined Science"), new Subject("  Combined\t Science  "));
        assertEquals(new SchoolingLevel("Primary 5"), new SchoolingLevel(" Primary   5 "));
        assertNotEquals(new Subject("Math"), new Subject("math"));
        assertEquals("A".repeat(40), new Subject("A".repeat(40)).value);
        assertEquals("A".repeat(30), new SchoolingLevel("A".repeat(30)).value);
        for (String text : new String[]{"", " ", "123", "Math/Science", "Math\n", "数学", "-Math"}) {
            assertThrows(IllegalArgumentException.class, () -> new Subject(text));
            assertThrows(IllegalArgumentException.class, () -> new SchoolingLevel(text));
        }
        assertThrows(IllegalArgumentException.class, () -> new Subject("A".repeat(41)));
        assertThrows(IllegalArgumentException.class, () -> new SchoolingLevel("A".repeat(31)));
        assertThrows(NullPointerException.class, () -> new Subject(null));
    }

    @Test
    public void rates_useDecimalValuesAndRejectInvalidPrecision() {
        assertEquals(new HourlyRate(new BigDecimal("45")), new HourlyRate(new BigDecimal("45.00")));
        assertEquals("0.00", new HourlyRate(BigDecimal.ZERO).toString());
        assertEquals(new HourlyRate(new BigDecimal("45")).hashCode(),
                new HourlyRate(new BigDecimal("45.00")).hashCode());
        assertThrows(IllegalArgumentException.class, () -> new HourlyRate(new BigDecimal("-0.01")));
        assertThrows(IllegalArgumentException.class, () -> new HourlyRate(new BigDecimal("45.001")));
    }

    @Test
    public void lessonSlot_rejectsOvernightEmptyOrSubMinuteRanges() {
        LocalTime ten = LocalTime.of(10, 0);
        LocalTime eleven = LocalTime.of(11, 0);
        assertEquals("MONDAY 10:00-11:00", new WeeklyLessonSlot(DayOfWeek.MONDAY, ten, eleven).toString());
        assertThrows(IllegalArgumentException.class, () -> new WeeklyLessonSlot(DayOfWeek.MONDAY, ten, ten));
        assertThrows(IllegalArgumentException.class, () -> new WeeklyLessonSlot(DayOfWeek.MONDAY, eleven, ten));
        assertThrows(IllegalArgumentException.class, () -> {
                    new WeeklyLessonSlot(DayOfWeek.MONDAY, ten.plusSeconds(1), eleven);
                });
        assertThrows(NullPointerException.class, () -> new WeeklyLessonSlot(null, ten, eleven));
    }

    @Test
    public void guardian_requiresBothDetails() {
        assertThrows(NullPointerException.class, () -> new GuardianContact(new Name("Janet"), null));
        assertThrows(NullPointerException.class, () -> new GuardianContact(null, new Phone("91234567")));
        assertEquals(new GuardianContact(new Name("Janet"), new Phone("91234567")),
                new GuardianContact(new Name("Janet"), new Phone("91234567")));
    }
}
