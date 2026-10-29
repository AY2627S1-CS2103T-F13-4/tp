package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/** Tests that data mutations retain search criteria and update results using the changed data. */
public class FilterPreservationTest {
    private static final Lesson MONDAY_MATH = new Lesson(new Subject("Math"),
            new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(16, 0), LocalTime.of(17, 0)),
            new HourlyRate(new BigDecimal("50")));
    private static final Lesson TUESDAY_ENGLISH = new Lesson(new Subject("English"),
            new WeeklyLessonSlot(DayOfWeek.TUESDAY, LocalTime.of(16, 0), LocalTime.of(17, 0)),
            new HourlyRate(new BigDecimal("50")));
    private static final Student ALICE_ADAMS = student("Alice Adams");
    private static final Student ALICE_BAKER = student("Alice Baker");
    private static final Student BOB_BROWN = student("Bob Brown");
    private static final Student ALICE_CLARK = student("Alice Clark").withArchived(true);
    private static final Student ALICE_DAVIS = student("Alice Davis").withArchived(true);
    private static final Student BOB_EVANS = student("Bob Evans").withArchived(true);
    private static final String ACTIVE_SEARCH = "find name/alice subject/math day/mon";

    private final Model model = new ModelManager(new AddressBookBuilder()
            .withPerson(ALICE_ADAMS).withPerson(ALICE_BAKER).withPerson(BOB_BROWN)
            .withPerson(ALICE_CLARK).withPerson(ALICE_DAVIS).withPerson(BOB_EVANS).build(), new UserPrefs());
    private final AddressBookParser parser = new AddressBookParser();

    private static Student student(String name) {
        return new PersonBuilder().withName(name).withLessons(MONDAY_MATH, TUESDAY_ENGLISH).build();
    }

    private void execute(String command) throws ParseException, CommandException {
        parser.parseCommand(command).execute(model);
    }

    @Test
    public void edit_matchingStudent_preservesStudentAndLessonFilters() throws Exception {
        execute(ACTIVE_SEARCH);
        Predicate<Lesson> lessonFilter = model.getLessonPredicate().getValue();

        execute("edit 1 phone/91234567");

        Student edited = new PersonBuilder(ALICE_ADAMS).withPhone("91234567").build();
        assertEquals(List.of(edited, ALICE_BAKER), model.getFilteredPersonList());
        assertSame(lessonFilter, model.getLessonPredicate().getValue());
        assertFalse(lessonFilter.test(TUESDAY_ENGLISH));
        assertFalse(model.isArchiveView());
    }

    @Test
    public void edit_archivedStudent_preservesBothListsFiltersAndArchiveView() throws Exception {
        execute(ACTIVE_SEARCH);
        Predicate<Lesson> lessonFilter = model.getLessonPredicate().getValue();
        model.setArchiveView(true);
        execute("find name/alice");

        execute("edit 1 name/Carol Clark");

        assertEquals(List.of(ALICE_DAVIS), model.getFilteredPersonList());
        assertEquals(List.of(ALICE_ADAMS, ALICE_BAKER), model.getFilteredActivePersonList());
        assertTrue(model.getAddressBook().getArchivedPersonList().contains(
                new PersonBuilder(ALICE_CLARK).withName("Carol Clark").build()));
        assertSame(lessonFilter, model.getLessonPredicate().getValue());
        assertTrue(model.isArchiveView());
    }

    @Test
    public void lessonEdit_lastMatchingLessonChanged_removesStudentAndRetainsFilters() throws Exception {
        execute(ACTIVE_SEARCH);
        Predicate<Lesson> lessonFilter = model.getLessonPredicate().getValue();

        execute("lesson-edit 1 1 rate/60");
        assertEquals(2, model.getFilteredPersonList().size());
        assertEquals(new HourlyRate(new BigDecimal("60")),
                model.getFilteredPersonList().getFirst().getLessons().getFirst().getHourlyRate());

        execute("lesson-edit 1 1 subject/English");

        assertEquals(List.of(ALICE_BAKER), model.getFilteredPersonList());
        assertSame(lessonFilter, model.getLessonPredicate().getValue());
        assertTrue(lessonFilter.test(MONDAY_MATH));
        assertFalse(lessonFilter.test(TUESDAY_ENGLISH));
        assertEquals(3, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void lessonEdit_anotherLessonMatches_keepsStudentInResults() throws Exception {
        Lesson morningMath = new Lesson(new Subject("Math"),
                new WeeklyLessonSlot(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                new HourlyRate(new BigDecimal("50")));
        Student original = new PersonBuilder(ALICE_ADAMS).withLessons(MONDAY_MATH, morningMath).build();
        model.setPerson(ALICE_ADAMS, original);
        execute(ACTIVE_SEARCH);
        Predicate<Lesson> lessonFilter = model.getLessonPredicate().getValue();

        execute("lesson-edit 1 1 subject/English");

        Student edited = new PersonBuilder(original).withLessons(
                new Lesson(new Subject("English"), MONDAY_MATH.getLessonSlot(), MONDAY_MATH.getHourlyRate()),
                morningMath).build();
        assertEquals(List.of(edited, ALICE_BAKER), model.getFilteredPersonList());
        assertSame(lessonFilter, model.getLessonPredicate().getValue());
    }

    @Test
    public void archive_filteredLists_updatesBothResultsAndRetainsLessonFilter() throws Exception {
        model.setArchiveView(true);
        execute("find name/alice");
        model.setArchiveView(false);
        execute(ACTIVE_SEARCH);
        Predicate<Lesson> lessonFilter = model.getLessonPredicate().getValue();

        execute("archive 1");

        assertEquals(List.of(ALICE_BAKER), model.getFilteredPersonList());
        assertEquals(List.of(ALICE_ADAMS.withArchived(true), ALICE_CLARK, ALICE_DAVIS),
                model.getFilteredArchivedPersonList());
        assertSame(lessonFilter, model.getLessonPredicate().getValue());
        assertFalse(model.isArchiveView());
    }

    @Test
    public void archive_nonmatchingDestinationSearch_keepsTransferredStudentHidden() throws Exception {
        model.setArchiveView(true);
        execute("find name/bob");
        model.setArchiveView(false);
        execute(ACTIVE_SEARCH);

        execute("archive 1");

        assertEquals(List.of(ALICE_BAKER), model.getFilteredPersonList());
        assertEquals(List.of(BOB_EVANS), model.getFilteredArchivedPersonList());
        assertTrue(model.getAddressBook().getArchivedPersonList().contains(ALICE_ADAMS.withArchived(true)));
    }

    @Test
    public void unarchive_filteredLists_updatesBothResultsAndKeepsArchiveView() throws Exception {
        execute(ACTIVE_SEARCH);
        Predicate<Lesson> lessonFilter = model.getLessonPredicate().getValue();
        model.setArchiveView(true);
        execute("find name/alice");

        execute("unarchive 1");

        assertEquals(List.of(ALICE_DAVIS), model.getFilteredPersonList());
        assertEquals(List.of(ALICE_ADAMS, ALICE_BAKER, ALICE_CLARK.withArchived(false)),
                model.getFilteredActivePersonList());
        assertSame(lessonFilter, model.getLessonPredicate().getValue());
        assertTrue(model.isArchiveView());
    }

    @Test
    public void unarchive_nonmatchingDestinationSearch_keepsTransferredStudentHidden() throws Exception {
        execute("find name/bob subject/english day/tue");
        Predicate<Lesson> lessonFilter = model.getLessonPredicate().getValue();
        model.setArchiveView(true);
        execute("find name/alice");

        execute("unarchive 1");

        assertEquals(List.of(ALICE_DAVIS), model.getFilteredPersonList());
        assertEquals(List.of(BOB_BROWN), model.getFilteredActivePersonList());
        assertTrue(model.getAddressBook().getPersonList().contains(ALICE_CLARK.withArchived(false)));
        assertSame(lessonFilter, model.getLessonPredicate().getValue());
        assertTrue(model.isArchiveView());
    }

    @Test
    public void add_nonmatchingStudent_clearsActiveSearchToShowNewStudent() throws Exception {
        // AddCommand selects the new student by its place in the shown list, so add clears the search.
        execute(ACTIVE_SEARCH);
        Student nonmatching = student("Carol Young");

        new AddCommand(nonmatching).execute(model);

        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
        assertTrue(model.getFilteredPersonList().contains(nonmatching));
        assertTrue(model.getLessonPredicate().getValue().test(TUESDAY_ENGLISH));
        assertFalse(model.isArchiveView());
    }

    @Test
    public void listCommands_afterMutations_explicitlyClearSelectedSearch() throws Exception {
        execute(ACTIVE_SEARCH);
        execute("archive 1");

        execute("list");

        assertEquals(List.of(ALICE_BAKER, BOB_BROWN), model.getFilteredPersonList());
        assertTrue(model.getLessonPredicate().getValue().test(TUESDAY_ENGLISH));
        execute("list-archive");
        execute("find name/alice");
        execute("unarchive 1");
        execute("list-archive");
        assertEquals(List.of(ALICE_CLARK, ALICE_DAVIS, BOB_EVANS), model.getFilteredPersonList());
    }
}
