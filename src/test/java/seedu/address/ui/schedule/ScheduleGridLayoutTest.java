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
    private static final double PIXEL_SNAP = 1;

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

            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:25", "09:45")));
            assertCardGeometry("Alice", 1, 1, 25, 15, 20);

            // Crossing the hour line: the card spans both rows and keeps its exact twenty minutes.
            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:50", "10:10")));
            assertCardGeometry("Alice", 1, 2, 50, 50, 20);
        });
    }

    // A lesson under 15 minutes would be too thin to read, so its block starts at the lesson and is
    // 15 minutes long, crossing the hour line if it must.
    @Test
    public void layout_lessonUnderFifteenMinutes_drawsShortestBlockFromStart() throws Exception {
        runOnFxThread(() -> {
            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:00", "09:01")));
            assertCardGeometry("Alice", 1, 1, 0, 45, 15);

            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:59", "10:00")));
            assertCardGeometry("Alice", 1, 2, 59, 46, 15);

            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "10:00", "10:14")));
            assertCardGeometry("Alice", 2, 1, 0, 45, 15);
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
            // The day ends at midnight, so a one-minute lesson at 11.58pm is drawn from 11.45pm.
            showStudents(FXCollections.observableArrayList(student("Late", DayOfWeek.SUNDAY, "23:58", "23:59")));
            assertEquals(15, timeLabels().size());
            assertEquals("11.00pm", timeLabels().get(14));
            assertCardGeometry("Late", 15, 1, 45, 0, 15);

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
                    student("First", DayOfWeek.MONDAY, "09:00", "09:15"),
                    student("Adjacent", DayOfWeek.MONDAY, "09:15", "09:30"),
                    student("Gapped", DayOfWeek.MONDAY, "09:35", "09:50")));

            assertCardGeometry("First", 1, 1, 0, 45, 15);
            assertCardGeometry("Adjacent", 1, 1, 15, 30, 15);
            assertCardGeometry("Gapped", 1, 1, 35, 10, 15);
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
                    student("Long", DayOfWeek.MONDAY, "09:00", "09:40"),
                    student("Short", DayOfWeek.MONDAY, "09:05", "09:20"),
                    student("Next", DayOfWeek.MONDAY, "09:20", "09:35")));

            assertCardGeometry("Long", 1, 1, 0, 20, 40);
            assertCardGeometry("Short", 1, 1, 5, 40, 15);
            assertCardGeometry("Next", 1, 1, 20, 25, 15);
            assertEquals(Integer.valueOf(1), GridPane.getColumnIndex(card("Long")));
            assertEquals(Integer.valueOf(2), GridPane.getColumnIndex(card("Short")));
            assertEquals(Integer.valueOf(2), GridPane.getColumnIndex(card("Next")));
            assertTrue(card("Long").getBoundsInParent().getMaxX() < card("Short").getBoundsInParent().getMinX());
            assertEquals(card("Short").getBoundsInParent().getMaxY(),
                    card("Next").getBoundsInParent().getMinY(), EPSILON);
            assertEquals(9, grid().getColumnConstraints().size());
        });
    }

    // A day with overlapping lessons widens to one lesson column per overlap; every block keeps the
    // normal width of one lesson column, including the ones that overlap nothing.
    @Test
    public void layout_overlapLaterInDay_widensDayAndKeepsBlocksOneColumnWide() throws Exception {
        runOnFxThread(() -> {
            showStudents(FXCollections.observableArrayList(
                    student("Morning", DayOfWeek.MONDAY, "10:00", "11:00"),
                    student("First", DayOfWeek.MONDAY, "14:00", "15:00"),
                    student("Second", DayOfWeek.MONDAY, "14:30", "15:30"),
                    student("Tuesday", DayOfWeek.TUESDAY, "10:00", "11:00")));

            for (String name : List.of("Morning", "First", "Second", "Tuesday")) {
                assertEquals(Integer.valueOf(1), GridPane.getColumnSpan(card(name)), name);
                // Column edges snap to whole pixels, so equal shares can differ by one pixel.
                assertEquals(card("Tuesday").getWidth(), card(name).getWidth(), PIXEL_SNAP);
            }
            assertEquals(card("First").getBoundsInParent().getMinX(), card("Morning").getBoundsInParent().getMinX(),
                    EPSILON);
        });
    }

    @Test
    public void layout_viewportResized_keepsMinuteScaleAndCardHeights() throws Exception {
        runOnFxThread(() -> {
            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:25", "09:45")));
            assertCardGeometry("Alice", 1, 1, 25, 15, 20);

            layoutViewport(1100, 1000);
            assertCardGeometry("Alice", 1, 1, 25, 15, 20);

            layoutViewport(350, 90);
            assertCardGeometry("Alice", 1, 1, 25, 15, 20);
        });
    }

    @Test
    public void source_changed_rebuildsExactGeometryAndOrigin() throws Exception {
        runOnFxThread(() -> {
            ObservableList<Student> students = FXCollections.observableArrayList(
                    student("Alice", DayOfWeek.MONDAY, "09:05", "09:20"));
            showStudents(students);
            assertCardGeometry("Alice", 1, 1, 5, 40, 15);

            students.add(student("Bob", DayOfWeek.TUESDAY, "08:59", "09:20"));
            layoutViewport(900, 700);
            assertCardGeometry("Alice", 2, 1, 5, 40, 15);
            assertCardGeometry("Bob", 1, 2, 59, 40, 21);

            students.set(0, student("Alice", DayOfWeek.MONDAY, "09:29", "09:51"));
            layoutViewport(900, 700);
            assertCardGeometry("Alice", 2, 1, 29, 9, 22);

            students.remove(1);
            layoutViewport(900, 700);
            assertCardGeometry("Alice", 1, 1, 29, 9, 22);

            students.clear();
            layoutViewport(900, 700);
            assertEquals(List.of("9.00am"), timeLabels().subList(0, 1));
            assertEquals(12, grid().getRowConstraints().size());
            assertTrue(grid().getChildren().stream().noneMatch(node -> node.getStyleClass().contains("lesson-card")));

            students.add(student("New", DayOfWeek.MONDAY, "09:00", "09:01"));
            layoutViewport(900, 700);
            assertCardGeometry("New", 1, 1, 0, 45, 15);
        });
    }

    // Issue #164: every block shows the number that commands use and whose lesson it is without hovering,
    // however short the lesson. Under 30 minutes there is room for one line, so it shows just those;
    // from 30 minutes the time fits underneath.
    @Test
    public void layout_lessonsUnderAnHour_showNumberAndStudentInsideBlock() throws Exception {
        runOnFxThread(() -> {
            for (String end : List.of("09:01", "09:15", "09:29", "09:30", "09:45", "09:59")) {
                showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:00", end)));
                Region card = card("Alice");
                assertTrue(card.getHeight() >= 15, end);
                String time = "9.00am-9." + end.substring(3) + "am";
                boolean hasTime = end.compareTo("09:30") >= 0;
                assertEquals(hasTime ? List.of("#1.1", "Alice", time) : List.of("#1.1", "Alice"), labelTexts(card),
                        end);
                if (hasTime) {
                    assertShownWhole(card, label(card, time), end);
                }
                assertShownWhole(card, label(card, "#1.1"), end);
                assertShownWhole(card, label(card, "Alice"), end);

                // In a narrow day the name gives way, never the number.
                layoutViewport(500, 700);
                assertShownWhole(card, label(card, "#1.1"), end);
            }
        });
    }

    @Test
    public void layout_lessonsOfAnHourOrMore_keepFullCard() throws Exception {
        runOnFxThread(() -> {
            for (String end : List.of("10:00", "11:00")) {
                showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "09:00", end)));
                Region card = card("Alice");
                String time = "9.00am-" + (end.equals("10:00") ? "10.00am" : "11.00am");
                assertEquals(List.of("Alice", time, "#1.1 \u00b7 Math"), labelTexts(card), end);
                for (String text : labelTexts(card)) {
                    assertShownWhole(card, label(card, text), end);
                }
            }
        });
    }

    // Only midnight stops a short block: one near the end of an evening grid starts at the lesson and the grid
    // grows to fit it, as it does for any lesson past 8pm.
    @Test
    public void layout_shortLessonNearEveningEnd_extendsGridInsteadOfMovingBlock() throws Exception {
        runOnFxThread(() -> {
            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "19:50", "19:55")));
            assertEquals("8.00pm", timeLabels().get(timeLabels().size() - 1));
            assertCardGeometry("Alice", 11, 2, 50, 55, 15);

            showStudents(FXCollections.observableArrayList(student("Alice", DayOfWeek.MONDAY, "20:50", "20:55")));
            assertEquals("9.00pm", timeLabels().get(timeLabels().size() - 1));
            assertCardGeometry("Alice", 12, 2, 50, 55, 15);
        });
    }

    // A one-minute lesson is drawn 15 minutes long, so a lesson starting under that block goes beside it
    // instead of covering it; lessons of 15 minutes or more still share a column back to back.
    @Test
    public void layout_lessonStartsUnderShortBlock_goesBesideIt() throws Exception {
        runOnFxThread(() -> {
            showStudents(FXCollections.observableArrayList(
                    student("One", DayOfWeek.MONDAY, "09:00", "09:01"),
                    student("Two", DayOfWeek.MONDAY, "09:01", "09:30"),
                    student("Three", DayOfWeek.MONDAY, "09:30", "09:45"),
                    student("Four", DayOfWeek.MONDAY, "09:45", "10:00")));

            assertEquals(Integer.valueOf(1), GridPane.getColumnIndex(card("One")));
            assertEquals(Integer.valueOf(2), GridPane.getColumnIndex(card("Two")));
            assertEquals(Integer.valueOf(1), GridPane.getColumnIndex(card("Three")));
            assertEquals(Integer.valueOf(1), GridPane.getColumnIndex(card("Four")));
            assertTrue(card("One").getBoundsInParent().getMaxX() < card("Two").getBoundsInParent().getMinX());
            assertEquals(card("Three").getBoundsInParent().getMaxY(),
                    card("Four").getBoundsInParent().getMinY(), EPSILON);
            assertEquals(9, grid().getColumnConstraints().size());
        });
    }

    /** Checks {@code label} is visible, inside {@code card}, and not cut short with an ellipsis. */
    private static void assertShownWhole(Region card, Label label, String message) {
        assertTrue(label.isVisible(), message);
        Bounds bounds = card.sceneToLocal(label.localToScene(label.getLayoutBounds()));
        assertTrue(bounds.getMinX() >= 0 && bounds.getMaxX() <= card.getWidth() + EPSILON, message);
        assertTrue(bounds.getMinY() >= 0 && bounds.getMaxY() <= card.getHeight() + EPSILON, message);
        assertTrue(label.getWidth() >= label.prefWidth(-1), message);
    }

    private static Label label(Region card, String text) {
        return card.lookupAll(".label").stream().map(Label.class::cast).filter(label -> label.getText().equals(text))
                .findFirst().orElseThrow();
    }

    private static List<String> labelTexts(Region card) {
        return card.lookupAll(".label").stream().map(Label.class::cast).map(Label::getText).toList();
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
