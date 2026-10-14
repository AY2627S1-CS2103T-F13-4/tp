package seedu.address.ui.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies minute-precision card geometry after managed layout with the application's stylesheet.
 */
public class ScheduleGridLayoutTest {
    private static final double EPSILON = 0.001;

    private ScheduleGrid schedule;
    private ScrollPane viewport;

    @BeforeAll
    public static void initializeJavaFx() throws Exception {
        FxTestUtil.initialize();
    }

    @AfterEach
    public void disposeSchedule() throws Exception {
        if (schedule != null) {
            runOnFxThread(schedule::dispose);
        }
    }

    @Test
    public void layout_partialIntervals_preservesExactLessonBounds() throws Exception {
        runOnFxThread(() -> {
            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:05", "09:20")));
            assertCardGeometry("Alice", 1, 1, 5, 40, 15);

            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:25", "09:35")));
            assertCardGeometry("Alice", 1, 1, 25, 25, 10);

            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:00", "09:01")));
            assertCardGeometry("Alice", 1, 1, 0, 59, 1);

            // Crossing the hour line: the card spans both rows and keeps its exact two minutes.
            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:59", "10:01")));
            assertCardGeometry("Alice", 1, 2, 59, 59, 2);

            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:59", "10:00")));
            assertCardGeometry("Alice", 1, 1, 59, 0, 1);

            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "10:00", "10:01")));
            assertCardGeometry("Alice", 2, 1, 0, 59, 1);
        });
    }

    // The calendar always shows 9am to 8pm, so a light week still reads as a working day,
    // and grows to fit any lesson outside those hours instead of hiding it.
    @Test
    public void layout_lessonBeforeNine_extendsGridEarlierAndAlignsOtherDays() throws Exception {
        runOnFxThread(() -> {
            showStudents(FXCollections.observableArrayList(
                    student("Early", DayOfWeek.MONDAY, "08:15", "08:45"),
                    student("Later", DayOfWeek.TUESDAY, "10:00", "10:30")));

            assertEquals(12, timeLabels().size());
            assertEquals(List.of("8.00am", "9.00am"), timeLabels().subList(0, 2));
            assertEquals("7.00pm", timeLabels().get(11));
            assertCardGeometry("Early", 1, 1, 15, 15, 30);
            assertCardGeometry("Later", 3, 1, 0, 30, 30);
            assertEquals(120, card("Later").getLayoutY() - grid().getCellBounds(1, 1).getMinY(), EPSILON);
        });
    }

    @Test
    public void layout_endOfDay_preservesFinalMinuteAndFullDayExtent() throws Exception {
        runOnFxThread(() -> {
            showStudents(FXCollections.observableArrayList(student("Late", DayOfWeek.SUNDAY, "23:58", "23:59")));
            assertEquals(15, timeLabels().size());
            assertEquals("11.00pm", timeLabels().get(14));
            assertCardGeometry("Late", 15, 1, 58, 1, 1);

            showStudents(FXCollections.observableArrayList(student("Full Day", DayOfWeek.MONDAY, "00:00", "23:59")));
            assertEquals(24, timeLabels().size());
            assertEquals("12.00am", timeLabels().get(0));
            assertEquals("11.00pm", timeLabels().get(23));
            assertEquals(25, grid().getRowConstraints().size());
            assertCardGeometry("Full Day", 1, 24, 0, 1, 1439);
        });
    }

    @Test
    public void layout_disjointLessonsWithinOneRow_sharesColumnWithoutVisualOverlap() throws Exception {
        runOnFxThread(() -> {
            showStudents(FXCollections.observableArrayList(
                    student("First", DayOfWeek.MONDAY, "09:00", "09:05"),
                    student("Adjacent", DayOfWeek.MONDAY, "09:05", "09:10"),
                    student("Gapped", DayOfWeek.MONDAY, "09:15", "09:20")));

            assertCardGeometry("First", 1, 1, 0, 55, 5);
            assertCardGeometry("Adjacent", 1, 1, 5, 50, 5);
            assertCardGeometry("Gapped", 1, 1, 15, 40, 5);
            assertEquals(GridPane.getColumnIndex(card("First")), GridPane.getColumnIndex(card("Adjacent")));
            assertEquals(GridPane.getColumnIndex(card("First")), GridPane.getColumnIndex(card("Gapped")));
            assertEquals(card("First").getBoundsInParent().getMaxY(),
                    card("Adjacent").getBoundsInParent().getMinY(), EPSILON);
            assertEquals(5, card("Gapped").getBoundsInParent().getMinY()
                    - card("Adjacent").getBoundsInParent().getMaxY(), EPSILON);
            assertEquals(8, grid().getColumnConstraints().size());
        });
    }

    @Test
    public void layout_overlappingLessonsWithinOneRow_separatesCardsAndReusesFreedColumn() throws Exception {
        runOnFxThread(() -> {
            showStudents(FXCollections.observableArrayList(
                    student("Long", DayOfWeek.MONDAY, "09:00", "09:20"),
                    student("Short", DayOfWeek.MONDAY, "09:05", "09:15"),
                    student("Next", DayOfWeek.MONDAY, "09:15", "09:25")));

            assertCardGeometry("Long", 1, 1, 0, 40, 20);
            assertCardGeometry("Short", 1, 1, 5, 45, 10);
            assertCardGeometry("Next", 1, 1, 15, 35, 10);
            assertEquals(Integer.valueOf(1), GridPane.getColumnIndex(card("Long")));
            assertEquals(Integer.valueOf(2), GridPane.getColumnIndex(card("Short")));
            assertEquals(Integer.valueOf(2), GridPane.getColumnIndex(card("Next")));
            assertTrue(card("Long").getBoundsInParent().getMaxX() < card("Short").getBoundsInParent().getMinX());
            assertEquals(card("Short").getBoundsInParent().getMaxY(),
                    card("Next").getBoundsInParent().getMinY(), EPSILON);
            assertEquals(9, grid().getColumnConstraints().size());
        });
    }

    // A lesson only shares its day's width with lessons it overlaps; at other times it fills the day.
    @Test
    public void layout_overlapLaterInDay_earlierLessonFillsWholeDay() throws Exception {
        runOnFxThread(() -> {
            showStudents(FXCollections.observableArrayList(
                    student("Morning", DayOfWeek.MONDAY, "10:00", "11:00"),
                    student("First", DayOfWeek.MONDAY, "14:00", "15:00"),
                    student("Second", DayOfWeek.MONDAY, "14:30", "15:30")));

            assertEquals(Integer.valueOf(2), GridPane.getColumnSpan(card("Morning")));
            assertEquals(Integer.valueOf(1), GridPane.getColumnSpan(card("First")));
            assertEquals(Integer.valueOf(1), GridPane.getColumnSpan(card("Second")));
            assertEquals(card("First").getBoundsInParent().getMinX(), card("Morning").getBoundsInParent().getMinX(),
                    EPSILON);
            assertEquals(card("Second").getBoundsInParent().getMaxX(), card("Morning").getBoundsInParent().getMaxX(),
                    EPSILON);
        });
    }

    @Test
    public void layout_viewportResized_keepsMinuteScaleAndCardHeights() throws Exception {
        runOnFxThread(() -> {
            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:25", "09:35")));
            assertCardGeometry("Alice", 1, 1, 25, 25, 10);

            layoutViewport(1100, 1000);
            assertCardGeometry("Alice", 1, 1, 25, 25, 10);

            layoutViewport(350, 90);
            assertCardGeometry("Alice", 1, 1, 25, 25, 10);
        });
    }

    @Test
    public void source_changed_rebuildsExactGeometryAndOrigin() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Student> students = FXCollections.observableArrayList(
                    student("Alice", DayOfWeek.MONDAY, "09:05", "09:20"));
            showStudents(students);
            assertCardGeometry("Alice", 1, 1, 5, 40, 15);

            students.add(student("Bob", DayOfWeek.TUESDAY, "08:59", "09:01"));
            layoutViewport(900, 700);
            assertCardGeometry("Alice", 2, 1, 5, 40, 15);
            assertCardGeometry("Bob", 1, 2, 59, 59, 2);

            students.set(0, student("Alice", DayOfWeek.MONDAY, "09:29", "09:31"));
            layoutViewport(900, 700);
            assertCardGeometry("Alice", 2, 1, 29, 29, 2);

            students.remove(1);
            layoutViewport(900, 700);
            assertCardGeometry("Alice", 1, 1, 29, 29, 2);

            students.clear();
            layoutViewport(900, 700);
            assertEquals(List.of("9.00am"), timeLabels().subList(0, 1));
            assertEquals(12, grid().getRowConstraints().size());
            assertTrue(grid().getChildren().stream().noneMatch(node -> node.getStyleClass().contains("lesson-card")));

            students.add(student("New", DayOfWeek.MONDAY, "09:00", "09:01"));
            layoutViewport(900, 700);
            assertCardGeometry("New", 1, 1, 0, 59, 1);
        });
    }

    private void showStudents(ObservableList<Student> students) {
        if (schedule != null) {
            schedule.dispose();
        }
        schedule = new ScheduleGrid(students);
        viewport = new ScrollPane(grid());
        viewport.setFitToWidth(true);
        viewport.setFitToHeight(true);
        Scene scene = new Scene(viewport, 900, 700);
        scene.getStylesheets().add(getClass().getResource("/view/LightTheme.css").toExternalForm());
        layoutViewport(900, 700);
    }

    private void layoutViewport(double width, double height) {
        viewport.resize(width, height);
        viewport.applyCss();
        viewport.layout();
        grid().layout();
    }

    private void assertCardGeometry(String name, int row, int span, double top, double bottom, double height) {
        Region card = card(name);
        assertTrue(card.isManaged());
        assertEquals(Integer.valueOf(row), GridPane.getRowIndex(card));
        assertEquals(Integer.valueOf(span), GridPane.getRowSpan(card));
        int column = GridPane.getColumnIndex(card);
        int lastColumn = column + GridPane.getColumnSpan(card) - 1;
        Bounds firstCell = grid().getCellBounds(column, row);
        Bounds lastCell = grid().getCellBounds(lastColumn, row + span - 1);
        for (int i = row; i < row + span; i++) {
            assertEquals(60, grid().getCellBounds(column, i).getHeight(), EPSILON);
        }
        assertEquals(firstCell.getMinY() + top, card.getLayoutY(), EPSILON);
        assertEquals(lastCell.getMaxY() - bottom, card.getLayoutY() + card.getHeight(), EPSILON);
        assertEquals(height, card.getHeight(), EPSILON);
        assertEquals(firstCell.getMinX() + 4, card.getLayoutX(), EPSILON);
        assertEquals(lastCell.getMaxX() - 4, card.getLayoutX() + card.getWidth(), EPSILON);

        Bounds visibleBounds = card.getBoundsInParent();
        Bounds clipBounds = card.localToParent(card.getClip().getBoundsInLocal());
        assertEquals(card.getLayoutY(), visibleBounds.getMinY(), EPSILON);
        assertEquals(card.getLayoutY() + height, visibleBounds.getMaxY(), EPSILON);
        assertEquals(visibleBounds.getMinY(), clipBounds.getMinY(), EPSILON);
        assertEquals(visibleBounds.getMaxY(), clipBounds.getMaxY(), EPSILON);
    }

    private GridPane grid() {
        return schedule.getRoot();
    }

    private Region card(String name) {
        return grid().getChildren().stream().filter(Region.class::isInstance).map(Region.class::cast)
                .filter(node -> node.getStyleClass().contains("lesson-card"))
                .filter(node -> node.getAccessibleText().split("\n")[0].endsWith(" " + name)).findFirst().orElseThrow();
    }

    private List<String> timeLabels() {
        return grid().getChildren().stream().filter(Label.class::isInstance).map(Label.class::cast)
                .filter(label -> Integer.valueOf(0).equals(GridPane.getColumnIndex(label)))
                .map(Label::getText).toList();
    }

    private static Student student(String name, DayOfWeek day, String start, String end) {
        Lesson lesson = new Lesson(new Subject("Math"),
                new WeeklyLessonSlot(day, LocalTime.parse(start), LocalTime.parse(end)),
                new HourlyRate(new BigDecimal("50")));
        return new PersonBuilder().withName(name).withLessons(lesson).build();
    }
}
