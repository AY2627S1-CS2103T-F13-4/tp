package seedu.address.ui.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.FxTestUtil.callOnFxThread;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

public class ScheduleGridTest {
    private ScheduleGrid schedule;

    @BeforeAll
    public static void setUpJavaFx() throws Exception {
        FxTestUtil.initialize();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (schedule != null) {
            runOnFxThread(schedule::dispose);
        }
    }

    @Test
    public void constructor_nullList_throwsNullPointerException() throws Exception {
        runOnFxThread(() -> assertThrows(NullPointerException.class, () -> new ScheduleGrid(null)));
    }

    @Test
    public void constructor_nullStudent_throwsNullPointerException() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Student> students = FXCollections.observableArrayList();
            students.add(null);
            assertThrows(NullPointerException.class, () -> new ScheduleGrid(students));
        });
    }

    @Test
    public void constructor_emptyList_displaysSevenDaysWithoutTimeRows() throws Exception {
        runOnFxThread(() -> {
            schedule = new ScheduleGrid(FXCollections.observableArrayList());
            assertEmptySchedule();
        });
    }

    @Test
    public void constructor_studentsWithoutLessons_displaysEmptySchedule() throws Exception {
        runOnFxThread(() -> {
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Alice"), createStudent("Bob")));
            assertEmptySchedule();
        });
    }

    @Test
    public void constructor_unsortedLessons_groupsAndOrdersCardsWithoutChangingSource() throws Exception {
        runOnFxThread(() -> {
            Student tuesday = createStudent("Tuesday Student", createLesson(DayOfWeek.TUESDAY, "09:00", "10:00"));
            Student late = createStudent("Late Student", createLesson(DayOfWeek.MONDAY, "11:00", "12:00"));
            Student early = createStudent("Early Student", createLesson(DayOfWeek.MONDAY, "09:00", "10:00"));
            ObservableList<Student> students = FXCollections.observableArrayList(tuesday, late, early);
            schedule = new ScheduleGrid(students);

            assertEquals(List.of(tuesday, late, early), students);
            assertEquals(List.of("Early Student", "Late Student", "Tuesday Student"), cardNames());
            assertCardPosition("Early Student", 1, 1, 2);
            assertCardPosition("Late Student", 1, 5, 2);
            assertCardPosition("Tuesday Student", 2, 1, 2);
            assertEquals(List.of("09:00", "09:30", "10:00", "10:30", "11:00", "11:30"), timeLabels());
            assertGridCounts(3, 8, 6);
        });
    }

    @Test
    public void constructor_identicalLessonsFromDifferentStudents_preservesBothCards() throws Exception {
        runOnFxThread(() -> {
            Lesson lesson = createLesson(DayOfWeek.MONDAY, "09:00", "10:00");
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Alice", lesson), createStudent("Bob", lesson)));

            assertEquals(List.of("Alice", "Bob"), cardNames());
            assertCardPosition("Alice", 1, 1, 2);
            assertCardPosition("Bob", 2, 1, 2);
            assertEquals(Integer.valueOf(2), GridPane.getColumnSpan(dayLabels().get(0)));
            assertGridCounts(2, 9, 2);
        });
    }

    @Test
    public void constructor_multipleFreeColumns_reusesLeftmostColumn() throws Exception {
        runOnFxThread(() -> {
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Left", createLesson(DayOfWeek.MONDAY, "09:00", "11:00")),
                    createStudent("Right", createLesson(DayOfWeek.MONDAY, "09:30", "10:30")),
                    createStudent("Next", createLesson(DayOfWeek.MONDAY, "11:30", "12:00"))));

            assertCardPosition("Left", 1, 1, 4);
            assertCardPosition("Right", 2, 2, 2);
            assertCardPosition("Next", 1, 6, 1);
            assertGridCounts(3, 9, 6);
        });
    }

    @Test
    public void constructor_adjacentLessons_reusesColumn() throws Exception {
        runOnFxThread(() -> {
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("First", createLesson(DayOfWeek.MONDAY, "09:00", "10:00")),
                    createStudent("Second", createLesson(DayOfWeek.MONDAY, "10:00", "11:00"))));

            assertCardPosition("First", 1, 1, 2);
            assertCardPosition("Second", 1, 3, 2);
            assertGridCounts(2, 8, 4);
        });
    }

    @Test
    public void constructor_nestedLessonEndsBeforeLongLesson_reusesFreedColumn() throws Exception {
        runOnFxThread(() -> {
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Long", createLesson(DayOfWeek.MONDAY, "09:00", "12:00")),
                    createStudent("Short", createLesson(DayOfWeek.MONDAY, "09:30", "10:00")),
                    createStudent("Later", createLesson(DayOfWeek.MONDAY, "10:30", "11:00"))));

            assertCardPosition("Long", 1, 1, 6);
            assertCardPosition("Short", 2, 2, 1);
            assertCardPosition("Later", 2, 4, 1);
            assertGridCounts(3, 9, 6);
        });
    }

    @Test
    public void constructor_endAfterHalfHour_roundsTimeRangeUp() throws Exception {
        runOnFxThread(() -> {
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Alice", createLesson(DayOfWeek.MONDAY, "09:00", "10:40"))));

            assertEquals(List.of("09:00", "09:30", "10:00", "10:30"), timeLabels());
            assertGridCounts(1, 8, 4);
        });
    }

    @Test
    public void constructor_lessonShorterThanInterval_displaysPositiveSpan() throws Exception {
        runOnFxThread(() -> {
            // Minute-precision slots are valid model inputs, including lessons shorter than thirty minutes.
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Alice", createLesson(DayOfWeek.MONDAY, "09:00", "09:15"))));

            assertEquals(1, cards().size());
            assertTrue(GridPane.getRowSpan(cards().get(0)) > 0);
        });
    }

    @Test
    public void constructor_unalignedEarliestTime_alignsCardsWithTimeLabels() throws Exception {
        runOnFxThread(() -> {
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Early", createLesson(DayOfWeek.MONDAY, "09:15", "09:45")),
                    createStudent("Later", createLesson(DayOfWeek.TUESDAY, "10:00", "11:00"))));

            assertEquals(List.of("09:00", "09:30", "10:00", "10:30"), timeLabels());
            assertCardPosition("Later", 2, 3, 2);
        });
    }

    @Test
    public void constructor_endOfDay_includesLastTimeInterval() throws Exception {
        runOnFxThread(() -> {
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Alice", createLesson(DayOfWeek.SUNDAY, "23:00", "23:59"))));

            assertEquals(List.of("23:00", "23:30"), timeLabels());
            assertGridCounts(1, 8, 2);
        });
    }

    @Test
    public void constructor_midnightStart_displaysFirstDayInterval() throws Exception {
        runOnFxThread(() -> {
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Alice", createLesson(DayOfWeek.MONDAY, "00:00", "00:30"))));

            assertEquals(List.of("00:00"), timeLabels());
            assertCardPosition("Alice", 1, 1, 1);
            assertGridCounts(1, 8, 1);
        });
    }

    @Test
    public void source_addReplaceRemoveClear_rebuildsWithoutAccumulatingContent() throws Exception {
        runOnFxThread(() -> {
            Student alice = createStudent("Alice", createLesson(DayOfWeek.MONDAY, "09:00", "10:00"));
            Student bob = createStudent("Bob", createLesson(DayOfWeek.MONDAY, "09:30", "10:30"));
            Student editedBob = createStudent("Updated Bob", createLesson(DayOfWeek.WEDNESDAY, "12:00", "13:00"));
            ObservableList<Student> students = FXCollections.observableArrayList(alice);
            schedule = new ScheduleGrid(students);
            ColumnConstraints timeColumn = schedule.getRoot().getColumnConstraints().get(0);
            RowConstraints headerRow = schedule.getRoot().getRowConstraints().get(0);

            students.add(bob);
            assertGridCounts(2, 9, 3);
            assertEquals(List.of("Alice", "Bob"), cardNames());

            students.set(1, editedBob);
            assertEquals(List.of("Alice", "Updated Bob"), cardNames());
            assertCardPosition("Updated Bob", 3, 7, 2);
            assertGridCounts(2, 8, 8);

            students.remove(alice);
            assertEquals(List.of("12:00", "12:30"), timeLabels());
            assertCardPosition("Updated Bob", 3, 1, 2);
            assertGridCounts(1, 8, 2);

            students.clear();
            assertEmptySchedule();
            students.add(alice);
            assertGridCounts(1, 8, 2);
            students.clear();
            assertEmptySchedule();
            assertSame(timeColumn, schedule.getRoot().getColumnConstraints().get(0));
            assertSame(headerRow, schedule.getRoot().getRowConstraints().get(0));
            assertTrue(schedule.getRoot().isGridLinesVisible());
        });
    }

    @Test
    public void source_filterChanges_rebuildsVisibleLessonsAndBounds() throws Exception {
        runOnFxThread(() -> {
            Student alice = createStudent("Alice", createLesson(DayOfWeek.MONDAY, "09:00", "10:00"));
            Student bob = createStudent("Bob", createLesson(DayOfWeek.FRIDAY, "14:00", "15:00"));
            FilteredList<Student> students = new FilteredList<>(FXCollections.observableArrayList(alice, bob));
            schedule = new ScheduleGrid(students);
            assertGridCounts(2, 8, 12);

            students.setPredicate(student -> student.equals(bob));
            assertEquals(List.of("Bob"), cardNames());
            assertEquals(List.of("14:00", "14:30"), timeLabels());
            assertCardPosition("Bob", 5, 1, 2);
            assertGridCounts(1, 8, 2);

            students.setPredicate(student -> false);
            assertEmptySchedule();
            students.setPredicate(student -> true);
            assertEquals(List.of("Alice", "Bob"), cardNames());
            assertGridCounts(2, 8, 12);
        });
    }

    @Test
    public void source_unmodifiableObservableView_stillReceivesChanges() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Student> students = FXCollections.observableArrayList();
            schedule = new ScheduleGrid(FXCollections.unmodifiableObservableList(students));
            assertEmptySchedule();

            students.add(createStudent("Alice", createLesson(DayOfWeek.MONDAY, "09:00", "10:00")));
            assertEquals(List.of("Alice"), cardNames());
            assertGridCounts(1, 8, 2);
        });
    }

    @Test
    public void source_backgroundNotification_refreshesOnFxThread() throws Exception {
        ObservableList<Student> students = FXCollections.observableArrayList();
        callOnFxThread(() -> {
            schedule = new ScheduleGrid(students);
            updateFromWorker(students);
            assertEmptySchedule();
            return null;
        });

        // This assertion is queued after the worker's refresh, without sleeps or concurrent list reads.
        runOnFxThread(() -> {
            assertEquals(List.of("Alice"), cardNames());
            assertGridCounts(1, 8, 2);
        });
    }

    @Test
    public void dispose_repeated_stopsObservingSource() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Student> students = FXCollections.observableArrayList(
                    createStudent("Alice", createLesson(DayOfWeek.MONDAY, "09:00", "10:00")));
            schedule = new ScheduleGrid(students);
            List<Node> originalChildren = List.copyOf(schedule.getRoot().getChildren());

            schedule.dispose();
            schedule.dispose();
            students.clear();

            assertEquals(originalChildren, schedule.getRoot().getChildren());
            assertEquals(List.of("Alice"), cardNames());
            assertGridCounts(1, 8, 2);
        });
    }

    @Test
    public void dispose_pendingRefresh_doesNotRenderAfterDisposal() throws Exception {
        ObservableList<Student> students = FXCollections.observableArrayList();
        callOnFxThread(() -> {
            schedule = new ScheduleGrid(students);
            updateFromWorker(students);
            schedule.dispose();
            return null;
        });

        runOnFxThread(this::assertEmptySchedule);
    }

    private static void updateFromWorker(ObservableList<Student> students) throws Exception {
        // The FX thread waits while this isolated source is modified, then processes the queued refresh.
        FutureTask<Void> update = new FutureTask<>(() -> {
            students.add(createStudent("Alice", createLesson(DayOfWeek.MONDAY, "09:00", "10:00")));
        }, null);
        Thread worker = new Thread(update, "schedule-source-update");
        worker.setDaemon(true);
        worker.start();
        update.get(5, TimeUnit.SECONDS);
    }

    private void assertEmptySchedule() {
        assertGridCounts(0, 8, 0);
        assertEquals(List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                dayLabels().stream().map(Label::getText).toList());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7),
                dayLabels().stream().map(GridPane::getColumnIndex).toList());
    }

    private void assertGridCounts(int cardCount, int columnCount, int timeLabelCount) {
        assertEquals(cardCount, cards().size());
        assertEquals(7, dayLabels().size());
        assertEquals(columnCount, schedule.getRoot().getColumnConstraints().size());
        assertEquals(timeLabelCount, timeLabels().size());
        assertEquals(timeLabelCount + 1, schedule.getRoot().getRowConstraints().size());
    }

    private void assertCardPosition(String name, int column, int row, int rowSpan) {
        Region card = cards().stream().filter(node -> node.getAccessibleText().startsWith(name + "\n"))
                .findFirst().orElseThrow();
        assertEquals(Integer.valueOf(column), GridPane.getColumnIndex(card), name + " column");
        assertEquals(Integer.valueOf(row), GridPane.getRowIndex(card), name + " row");
        assertEquals(Integer.valueOf(rowSpan), GridPane.getRowSpan(card), name + " row span");
    }

    private List<Region> cards() {
        return schedule.getRoot().getChildren().stream().filter(Region.class::isInstance).map(Region.class::cast)
                .filter(node -> node.getStyleClass().contains("lesson-card")).toList();
    }

    private List<String> cardNames() {
        return cards().stream().map(card -> card.getAccessibleText().split("\n", 2)[0]).toList();
    }

    private List<Label> dayLabels() {
        return schedule.getRoot().getChildren().stream().filter(Label.class::isInstance).map(Label.class::cast)
                .filter(label -> Integer.valueOf(0).equals(GridPane.getRowIndex(label))).toList();
    }

    private List<String> timeLabels() {
        return schedule.getRoot().getChildren().stream().filter(Label.class::isInstance).map(Label.class::cast)
                .filter(label -> Integer.valueOf(0).equals(GridPane.getColumnIndex(label)))
                .map(Label::getText).toList();
    }

    private static Student createStudent(String name, Lesson... lessons) {
        return new PersonBuilder().withName(name).withLessons(lessons).build();
    }

    private static Lesson createLesson(DayOfWeek day, String start, String end) {
        return new Lesson(new Subject("Math"),
                new WeeklyLessonSlot(day, LocalTime.parse(start), LocalTime.parse(end)),
                new HourlyRate(new BigDecimal("50")));
    }
}
