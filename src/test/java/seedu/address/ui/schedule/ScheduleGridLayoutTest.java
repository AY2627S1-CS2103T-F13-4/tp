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
            assertCardGeometry("Alice", 1, 1, 10, 20, 30);

            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:25", "09:35")));
            assertCardGeometry("Alice", 1, 2, 50, 50, 20);

            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:00", "09:01")));
            assertCardGeometry("Alice", 1, 1, 0, 58, 2);

            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:29", "09:31")));
            assertCardGeometry("Alice", 1, 2, 58, 58, 4);

            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:29", "09:30")));
            assertCardGeometry("Alice", 1, 1, 58, 0, 2);

            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:30", "09:31")));
            assertCardGeometry("Alice", 1, 1, 0, 58, 2);
        });
    }

    @Test
    public void layout_unalignedEarliestTime_alignsOtherDaysWithRoundedOrigin() throws Exception {
        runOnFxThread(() -> {
            showStudents(FXCollections.observableArrayList(
                    student("Early", DayOfWeek.MONDAY, "09:15", "09:45"),
                    student("Later", DayOfWeek.TUESDAY, "10:00", "10:30")));

            assertEquals(List.of("09:00", "09:30", "10:00"), timeLabels());
            assertCardGeometry("Early", 1, 2, 30, 30, 60);
            assertCardGeometry("Later", 3, 1, 0, 0, 60);
            assertEquals(120, card("Later").getLayoutY() - grid().getCellBounds(1, 1).getMinY(), EPSILON);
        });
    }

    @Test
    public void layout_endOfDay_preservesFinalMinuteAndFullDayExtent() throws Exception {
        runOnFxThread(() -> {
            showStudents(FXCollections.observableArrayList(student("Late", DayOfWeek.SUNDAY, "23:58", "23:59")));
            assertEquals(List.of("23:30"), timeLabels());
            assertCardGeometry("Late", 1, 1, 56, 2, 2);

            showStudents(FXCollections.observableArrayList(student("Full Day", DayOfWeek.MONDAY, "00:00", "23:59")));
            assertEquals(48, timeLabels().size());
            assertEquals("00:00", timeLabels().get(0));
            assertEquals("23:30", timeLabels().get(47));
            assertEquals(49, grid().getRowConstraints().size());
            assertCardGeometry("Full Day", 1, 48, 0, 2, 2878);
        });
    }

    @Test
    public void layout_disjointLessonsWithinOneRow_sharesColumnWithoutVisualOverlap() throws Exception {
        runOnFxThread(() -> {
            showStudents(FXCollections.observableArrayList(
                    student("First", DayOfWeek.MONDAY, "09:00", "09:05"),
                    student("Adjacent", DayOfWeek.MONDAY, "09:05", "09:10"),
                    student("Gapped", DayOfWeek.MONDAY, "09:15", "09:20")));

            assertCardGeometry("First", 1, 1, 0, 50, 10);
            assertCardGeometry("Adjacent", 1, 1, 10, 40, 10);
            assertCardGeometry("Gapped", 1, 1, 30, 20, 10);
            assertEquals(GridPane.getColumnIndex(card("First")), GridPane.getColumnIndex(card("Adjacent")));
            assertEquals(GridPane.getColumnIndex(card("First")), GridPane.getColumnIndex(card("Gapped")));
            assertEquals(card("First").getBoundsInParent().getMaxY(),
                    card("Adjacent").getBoundsInParent().getMinY(), EPSILON);
            assertEquals(10, card("Gapped").getBoundsInParent().getMinY()
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

            assertCardGeometry("Long", 1, 1, 0, 20, 40);
            assertCardGeometry("Short", 1, 1, 10, 30, 20);
            assertCardGeometry("Next", 1, 1, 30, 10, 20);
            assertEquals(Integer.valueOf(1), GridPane.getColumnIndex(card("Long")));
            assertEquals(Integer.valueOf(2), GridPane.getColumnIndex(card("Short")));
            assertEquals(Integer.valueOf(2), GridPane.getColumnIndex(card("Next")));
            assertTrue(card("Long").getBoundsInParent().getMaxX() < card("Short").getBoundsInParent().getMinX());
            assertEquals(card("Short").getBoundsInParent().getMaxY(),
                    card("Next").getBoundsInParent().getMinY(), EPSILON);
            assertEquals(9, grid().getColumnConstraints().size());
        });
    }

    @Test
    public void layout_viewportResized_keepsMinuteScaleAndCardHeights() throws Exception {
        runOnFxThread(() -> {
            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:25", "09:35")));
            assertCardGeometry("Alice", 1, 2, 50, 50, 20);

            layoutViewport(1100, 1000);
            assertCardGeometry("Alice", 1, 2, 50, 50, 20);

            layoutViewport(350, 90);
            assertCardGeometry("Alice", 1, 2, 50, 50, 20);
        });
    }

    @Test
    public void source_changed_rebuildsExactGeometryAndRoundedOrigin() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Student> students = FXCollections.observableArrayList(
                    student("Alice", DayOfWeek.MONDAY, "09:05", "09:20"));
            showStudents(students);
            assertCardGeometry("Alice", 1, 1, 10, 20, 30);

            students.add(student("Bob", DayOfWeek.TUESDAY, "08:59", "09:01"));
            layoutViewport(900, 700);
            assertCardGeometry("Alice", 2, 1, 10, 20, 30);
            assertCardGeometry("Bob", 1, 2, 58, 58, 4);

            students.set(0, student("Alice", DayOfWeek.MONDAY, "09:29", "09:31"));
            layoutViewport(900, 700);
            assertCardGeometry("Alice", 2, 2, 58, 58, 4);

            students.remove(1);
            layoutViewport(900, 700);
            assertCardGeometry("Alice", 1, 2, 58, 58, 4);

            students.clear();
            layoutViewport(900, 700);
            assertEquals(1, grid().getRowConstraints().size());
            assertTrue(timeLabels().isEmpty());
            assertTrue(grid().getChildren().stream().noneMatch(node -> node.getStyleClass().contains("lesson-card")));

            students.add(student("New", DayOfWeek.MONDAY, "09:00", "09:01"));
            layoutViewport(900, 700);
            assertCardGeometry("New", 1, 1, 0, 58, 2);
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
        scene.getStylesheets().add(getClass().getResource("/view/DarkTheme.css").toExternalForm());
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
        Bounds firstCell = grid().getCellBounds(column, row);
        Bounds lastCell = grid().getCellBounds(column, row + span - 1);
        for (int i = row; i < row + span; i++) {
            assertEquals(60, grid().getCellBounds(column, i).getHeight(), EPSILON);
        }
        assertEquals(firstCell.getMinY() + top, card.getLayoutY(), EPSILON);
        assertEquals(lastCell.getMaxY() - bottom, card.getLayoutY() + card.getHeight(), EPSILON);
        assertEquals(height, card.getHeight(), EPSILON);
        assertEquals(firstCell.getMinX() + 3, card.getLayoutX(), EPSILON);
        assertEquals(firstCell.getMaxX() - 5, card.getLayoutX() + card.getWidth(), EPSILON);

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
                .filter(node -> node.getAccessibleText().startsWith(name + "\n")).findFirst().orElseThrow();
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
