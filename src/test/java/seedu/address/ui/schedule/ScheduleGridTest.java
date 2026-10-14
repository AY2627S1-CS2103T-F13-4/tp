package seedu.address.ui.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.FxTestUtil.callOnFxThread;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
import javafx.scene.layout.HBox;
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
    /** Rows from 9am to 8pm, which the calendar always shows. */
    private static final int WORKING_HOURS = 11;
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
    public void constructor_emptyList_displaysSevenDaysAndWorkingHours() throws Exception {
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
            assertCardPosition("Early Student", 1, 1, 1);
            assertCardPosition("Late Student", 1, 3, 1);
            assertCardPosition("Tuesday Student", 2, 1, 1);
            assertEquals("9.00am", timeLabels().get(0));
            assertEquals("7.00pm", timeLabels().get(10));
            assertGridCounts(3, 8, WORKING_HOURS);
        });
    }

    @Test
    public void constructor_identicalLessonsFromDifferentStudents_preservesBothCards() throws Exception {
        runOnFxThread(() -> {
            Lesson lesson = createLesson(DayOfWeek.MONDAY, "09:00", "10:00");
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Alice", lesson), createStudent("Bob", lesson)));

            assertEquals(List.of("Alice", "Bob"), cardNames());
            assertCardPosition("Alice", 1, 1, 1);
            assertCardPosition("Bob", 2, 1, 1);
            assertEquals(Integer.valueOf(2), GridPane.getColumnSpan(dayHeadings().get(0)));
            assertGridCounts(2, 9, WORKING_HOURS);
        });
    }

    @Test
    public void constructor_multipleFreeColumns_reusesLeftmostColumn() throws Exception {
        runOnFxThread(() -> {
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Left", createLesson(DayOfWeek.MONDAY, "09:00", "11:00")),
                    createStudent("Right", createLesson(DayOfWeek.MONDAY, "09:30", "10:30")),
                    createStudent("Next", createLesson(DayOfWeek.MONDAY, "11:30", "12:00"))));

            assertCardPosition("Left", 1, 1, 2);
            assertCardPosition("Right", 2, 1, 2);
            assertCardPosition("Next", 1, 3, 1);
            // Left shares the day with Right; Next has the day to itself, so it fills both columns.
            assertEquals(Integer.valueOf(1), GridPane.getColumnSpan(card("Left")));
            assertEquals(Integer.valueOf(1), GridPane.getColumnSpan(card("Right")));
            assertEquals(Integer.valueOf(2), GridPane.getColumnSpan(card("Next")));
            assertGridCounts(3, 9, WORKING_HOURS);
        });
    }

    @Test
    public void constructor_adjacentLessons_reusesColumn() throws Exception {
        runOnFxThread(() -> {
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("First", createLesson(DayOfWeek.MONDAY, "09:00", "10:00")),
                    createStudent("Second", createLesson(DayOfWeek.MONDAY, "10:00", "11:00"))));

            assertCardPosition("First", 1, 1, 1);
            assertCardPosition("Second", 1, 2, 1);
            assertGridCounts(2, 8, WORKING_HOURS);
        });
    }

    @Test
    public void constructor_nestedLessonEndsBeforeLongLesson_reusesFreedColumn() throws Exception {
        runOnFxThread(() -> {
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Long", createLesson(DayOfWeek.MONDAY, "09:00", "12:00")),
                    createStudent("Short", createLesson(DayOfWeek.MONDAY, "09:30", "10:00")),
                    createStudent("Later", createLesson(DayOfWeek.MONDAY, "10:30", "11:00"))));

            assertCardPosition("Long", 1, 1, 3);
            assertCardPosition("Short", 2, 1, 1);
            assertCardPosition("Later", 2, 2, 1);
            assertGridCounts(3, 9, WORKING_HOURS);
        });
    }

    @Test
    public void constructor_endAfterEightPm_roundsTimeRangeUpToNextHour() throws Exception {
        runOnFxThread(() -> {
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Alice", createLesson(DayOfWeek.MONDAY, "19:00", "20:40"))));

            assertEquals("8.00pm", timeLabels().get(WORKING_HOURS));
            assertGridCounts(1, 8, WORKING_HOURS + 1);
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
    public void constructor_lessonBeforeNine_startsAtItsHourAndAlignsOtherDays() throws Exception {
        runOnFxThread(() -> {
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Early", createLesson(DayOfWeek.MONDAY, "08:15", "08:45")),
                    createStudent("Later", createLesson(DayOfWeek.TUESDAY, "10:00", "11:00"))));

            assertEquals(List.of("8.00am", "9.00am", "10.00am"), timeLabels().subList(0, 3));
            assertCardPosition("Early", 1, 1, 1);
            assertCardPosition("Later", 2, 3, 1);
        });
    }

    @Test
    public void constructor_endOfDay_includesLastTimeInterval() throws Exception {
        runOnFxThread(() -> {
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Alice", createLesson(DayOfWeek.SUNDAY, "23:00", "23:59"))));

            assertEquals("11.00pm", timeLabels().get(14));
            assertGridCounts(1, 8, 15);
        });
    }

    @Test
    public void constructor_midnightStart_displaysFirstDayInterval() throws Exception {
        runOnFxThread(() -> {
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Alice", createLesson(DayOfWeek.MONDAY, "00:00", "00:30"))));

            assertEquals("12.00am", timeLabels().get(0));
            assertCardPosition("Alice", 1, 1, 1);
            assertGridCounts(1, 8, 20);
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
            assertGridCounts(2, 9, WORKING_HOURS);
            assertEquals(List.of("Alice", "Bob"), cardNames());

            students.set(1, editedBob);
            assertEquals(List.of("Alice", "Updated Bob"), cardNames());
            assertCardPosition("Updated Bob", 3, 4, 1);
            assertGridCounts(2, 8, WORKING_HOURS);

            students.remove(alice);
            assertCardPosition("Updated Bob", 3, 4, 1);
            assertGridCounts(1, 8, WORKING_HOURS);

            students.clear();
            assertEmptySchedule();
            students.add(alice);
            assertGridCounts(1, 8, WORKING_HOURS);
            students.clear();
            assertEmptySchedule();
            assertSame(timeColumn, schedule.getRoot().getColumnConstraints().get(0));
            assertSame(headerRow, schedule.getRoot().getRowConstraints().get(0));
        });
    }

    @Test
    public void source_filterChanges_rebuildsVisibleLessonsAndBounds() throws Exception {
        runOnFxThread(() -> {
            Student alice = createStudent("Alice", createLesson(DayOfWeek.MONDAY, "09:00", "10:00"));
            Student bob = createStudent("Bob", createLesson(DayOfWeek.FRIDAY, "14:00", "15:00"));
            FilteredList<Student> students = new FilteredList<>(FXCollections.observableArrayList(alice, bob));
            schedule = new ScheduleGrid(students);
            assertGridCounts(2, 8, WORKING_HOURS);

            students.setPredicate(student -> student.equals(bob));
            assertEquals(List.of("Bob"), cardNames());
            assertCardPosition("Bob", 5, 6, 1);
            assertGridCounts(1, 8, WORKING_HOURS);

            students.setPredicate(student -> false);
            assertEmptySchedule();
            students.setPredicate(student -> true);
            assertEquals(List.of("Alice", "Bob"), cardNames());
            assertGridCounts(2, 8, WORKING_HOURS);
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
            assertGridCounts(1, 8, WORKING_HOURS);
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
            assertGridCounts(1, 8, WORKING_HOURS);
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
            assertGridCounts(1, 8, WORKING_HOURS);
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

    // The headings show this week's dates and mark today; the line marks the current time.
    // A fixed clock keeps this independent of when and where the test runs.
    @Test
    public void constructor_fixedClock_showsWeekDatesTodayAndCurrentTime() throws Exception {
        runOnFxThread(() -> {
            Clock wednesdayMorning = Clock.fixed(Instant.parse("2026-10-14T10:30:00Z"), ZoneOffset.UTC);
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Alice", createLesson(DayOfWeek.MONDAY, "09:00", "10:00"))), wednesdayMorning);

            assertEquals(List.of("12", "13", "14", "15", "16", "17", "18"), dayNumbers());
            List<HBox> today = dayHeadings().stream()
                    .filter(heading -> heading.getStyleClass().contains("schedule-today")).toList();
            assertEquals(List.of("Wed"), today.stream()
                    .map(heading -> ((Label) heading.getChildren().get(0)).getText()).toList());

            assertEquals(List.of(3), todayColumns());

            Node nowLine = schedule.getRoot().getChildren().stream()
                    .filter(node -> node.getStyleClass().contains("schedule-now-line")).findFirst().orElseThrow();
            assertEquals(Integer.valueOf(3), GridPane.getColumnIndex(nowLine));
            assertEquals(Integer.valueOf(2), GridPane.getRowIndex(nowLine));
            assertEquals(30, GridPane.getMargin(nowLine).getTop(), 0.001);
        });
    }

    // On a Sunday the week shown is the one ending today, not the one starting tomorrow.
    @Test
    public void constructor_sundayClock_showsWeekEndingToday() throws Exception {
        runOnFxThread(() -> {
            Clock sunday = Clock.fixed(Instant.parse("2026-10-18T12:00:00Z"), ZoneOffset.UTC);
            schedule = new ScheduleGrid(FXCollections.observableArrayList(), sunday);

            assertEquals(List.of("12", "13", "14", "15", "16", "17", "18"), dayNumbers());
            assertEquals(List.of(7), todayColumns());
        });
    }

    // Left open past midnight, the calendar moves today's shading and, after Sunday, the dates.
    @Test
    public void updateForTime_dateChanges_redrawsTodayAndDates() throws Exception {
        runOnFxThread(() -> {
            MovableClock clock = new MovableClock(Instant.parse("2026-10-18T23:59:00Z"));
            schedule = new ScheduleGrid(FXCollections.observableArrayList(
                    createStudent("Alice", createLesson(DayOfWeek.MONDAY, "09:00", "10:00"))), clock);
            assertEquals(List.of(7), todayColumns());

            clock.now = Instant.parse("2026-10-19T00:01:00Z");
            schedule.updateForTime();
            assertEquals(List.of(1), todayColumns());
            assertEquals(List.of("19", "20", "21", "22", "23", "24", "25"), dayNumbers());
            assertEquals(List.of("Alice"), cardNames());
        });
    }

    // Outside the drawn hours there is nowhere to put the line, so it is left out.
    @Test
    public void constructor_currentTimeOutsideHours_hasNoCurrentTimeLine() throws Exception {
        runOnFxThread(() -> {
            Clock lateNight = Clock.fixed(Instant.parse("2026-10-14T23:30:00Z"), ZoneOffset.UTC);
            schedule = new ScheduleGrid(FXCollections.observableArrayList(), lateNight);

            assertTrue(schedule.getRoot().getChildren().stream()
                    .noneMatch(node -> node.getStyleClass().contains("schedule-now-line")));
        });
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
        assertGridCounts(0, 8, WORKING_HOURS);
        assertEquals(List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                dayHeadings().stream().map(heading -> ((Label) heading.getChildren().get(0)).getText()).toList());
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7),
                dayHeadings().stream().map(GridPane::getColumnIndex).toList());
    }

    private void assertGridCounts(int cardCount, int columnCount, int timeLabelCount) {
        assertEquals(cardCount, cards().size());
        assertEquals(7, dayHeadings().size());
        assertEquals(columnCount, schedule.getRoot().getColumnConstraints().size());
        assertEquals(timeLabelCount, timeLabels().size());
        assertEquals(timeLabelCount + 1, schedule.getRoot().getRowConstraints().size());
    }

    private void assertCardPosition(String name, int column, int row, int rowSpan) {
        Region card = card(name);
        assertEquals(Integer.valueOf(column), GridPane.getColumnIndex(card), name + " column");
        assertEquals(Integer.valueOf(row), GridPane.getRowIndex(card), name + " row");
        assertEquals(Integer.valueOf(rowSpan), GridPane.getRowSpan(card), name + " row span");
    }

    private List<Region> cards() {
        return schedule.getRoot().getChildren().stream().filter(Region.class::isInstance).map(Region.class::cast)
                .filter(node -> node.getStyleClass().contains("lesson-card")).toList();
    }

    private List<String> cardNames() {
        return cards().stream().map(ScheduleGridTest::cardName).toList();
    }

    /** Returns the student's name from a card's details, which start with its number, e.g. "#1.1 Alice". */
    private static String cardName(Region card) {
        return card.getAccessibleText().split("\n", 2)[0].split(" ", 2)[1];
    }

    private List<String> dayNumbers() {
        return dayHeadings().stream().map(heading -> ((Label) heading.getChildren().get(1)).getText()).toList();
    }

    /** Returns the first column of each day shaded as today; there should be exactly one. */
    private List<Integer> todayColumns() {
        return schedule.getRoot().getChildren().stream()
                .filter(node -> node.getStyleClass().contains("schedule-today-column"))
                .map(GridPane::getColumnIndex).toList();
    }

    private Region card(String name) {
        return cards().stream().filter(node -> cardName(node).equals(name)).findFirst().orElseThrow();
    }

    /** A clock that a test can move, to check what the calendar does when the date changes. */
    private static final class MovableClock extends Clock {
        private Instant now;

        MovableClock(Instant now) {
            this.now = now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private List<HBox> dayHeadings() {
        return schedule.getRoot().getChildren().stream().filter(HBox.class::isInstance).map(HBox.class::cast)
                .filter(heading -> Integer.valueOf(0).equals(GridPane.getRowIndex(heading))).toList();
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
