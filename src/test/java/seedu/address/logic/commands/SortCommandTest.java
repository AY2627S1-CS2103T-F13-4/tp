package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.RateSortOrder;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.PersonBuilder;

public class SortCommandTest {

    private static Lesson lesson(String rate) {
        return new Lesson(new Subject("Math"),
                new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0)),
                new HourlyRate(new BigDecimal(rate)));
    }

    private static Student student(String name, String level, String... rates) {
        Lesson[] lessons = java.util.Arrays.stream(rates).map(SortCommandTest::lesson).toArray(Lesson[]::new);
        PersonBuilder builder = new PersonBuilder().withName(name).withLessons(lessons);
        if (level != null) {
            builder.withSchoolingLevel(new SchoolingLevel(level));
        }
        return builder.build();
    }

    /** Added order: Ann(P3, $50), Bob(S1, $20), Cy(P4, $30), Di(P5, none), Eve(P6, $40 and $80), Fay(P1, $30). */
    private Model modelWithStudents() {
        AddressBook book = new AddressBook();
        for (Student s : List.of(student("Ann", "P3", "50"), student("Bob", "S1", "20"),
                student("Cy", "P4", "30"), student("Di", "P5"), student("Eve", "P6", "40", "80"),
                student("Fay", "P1", "30"))) {
            book.addPerson(s);
        }
        return new ModelManager(book, new UserPrefs());
    }

    private static List<String> names(Model model) {
        return model.getFilteredPersonList().stream().map(s -> s.getName().fullName).toList();
    }

    @Test
    public void execute_ascending_sortsWithinStageUsingLowestRate() {
        Model model = modelWithStudents();
        CommandResult result = new SortCommand(RateSortOrder.ASCENDING).execute(model);
        // Primary: Cy 30, Fay 30 (added order breaks the tie), Eve 40, Ann 50, Di (no lessons) last.
        assertEquals(List.of("Cy", "Fay", "Eve", "Ann", "Di", "Bob"), names(model));
        assertEquals(SortCommand.MESSAGE_ASCENDING, result.getFeedbackToUser());
    }

    @Test
    public void execute_descending_sortsWithinStageUsingHighestRate() {
        Model model = modelWithStudents();
        CommandResult result = new SortCommand(RateSortOrder.DESCENDING).execute(model);
        // Primary: Eve 80, Ann 50, Cy 30, Fay 30, Di (no lessons) still last.
        assertEquals(List.of("Eve", "Ann", "Cy", "Fay", "Di", "Bob"), names(model));
        assertEquals(SortCommand.MESSAGE_DESCENDING, result.getFeedbackToUser());
    }

    @Test
    public void execute_afterListCommand_restoresAddedOrderWithinStage() {
        Model model = modelWithStudents();
        new SortCommand(RateSortOrder.DESCENDING).execute(model);
        new ListCommand().execute(model);
        assertEquals(RateSortOrder.NONE, model.getRateSortOrder().getValue());
        assertEquals(List.of("Ann", "Cy", "Di", "Eve", "Fay", "Bob"), names(model));
    }

    @Test
    public void execute_editedStudent_resortsImmediately() {
        Model model = modelWithStudents();
        new SortCommand(RateSortOrder.ASCENDING).execute(model);
        Student ann = model.getFilteredPersonList().stream()
                .filter(s -> s.getName().fullName.equals("Ann")).findFirst().orElseThrow();
        model.setPerson(ann, new PersonBuilder(ann).withLessons(lesson("10")).build());
        assertEquals("Ann", names(model).get(0));
    }

    @Test
    public void constructor_invalidOrder_throws() {
        assertThrows(NullPointerException.class, () -> new SortCommand(null));
        assertThrows(AssertionError.class, () -> new SortCommand(RateSortOrder.NONE));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SortCommand(RateSortOrder.ASCENDING).execute(null));
    }

    @Test
    public void equals() {
        SortCommand asc = new SortCommand(RateSortOrder.ASCENDING);
        assertEquals(asc, asc);
        assertEquals(asc, new SortCommand(RateSortOrder.ASCENDING));
        assertEquals(asc.hashCode(), new SortCommand(RateSortOrder.ASCENDING).hashCode());
        assertNotEquals(asc, new SortCommand(RateSortOrder.DESCENDING));
        assertNotEquals(asc, new ListCommand());
        assertNotEquals(asc, null);
    }
}
