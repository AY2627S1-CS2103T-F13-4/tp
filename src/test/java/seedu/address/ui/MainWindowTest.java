package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Student;
import seedu.address.model.person.Subject;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.FxTestUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Verifies schedule wiring and listener cleanup through the real main-window FXML and lifecycle.
 */
public class MainWindowTest {
    @TempDir
    public Path temporaryFolder;

    private List<Window> existingWindows;
    private ModelManager model;
    private MainWindow mainWindow;
    private Stage stage;

    @BeforeAll
    public static void initializeJavaFx() throws Exception {
        FxTestUtil.initialize();
    }

    @BeforeEach
    public void setUp() throws Exception {
        runOnFxThread(() -> {
            existingWindows = List.copyOf(Window.getWindows());
            model = new ModelManager();
            model.addPerson(student("Alice", DayOfWeek.MONDAY));
            model.addPerson(student("Bob", DayOfWeek.TUESDAY));
            Path dataFile = temporaryFolder.resolve("students.json");
            LogicManager logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataFile),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
            stage = new Stage();
            stage.setOpacity(0);
            mainWindow = new MainWindow(stage, logic, dataFile);
            mainWindow.fillInnerParts();
            mainWindow.show();
        });
    }

    @AfterEach
    public void closeWindows() throws Exception {
        runOnFxThread(() -> {
            if (stage != null) {
                stage.close();
            }
            if (existingWindows != null) {
                for (Window window : List.copyOf(Window.getWindows())) {
                    if (!existingWindows.contains(window)) {
                        window.hide();
                    }
                }
            }
        });
    }

    @Test
    public void fillInnerParts_actualFxml_attachesScheduleToPlaceholder() throws Exception {
        runOnFxThread(() -> {
            ScrollPane placeholder = schedulePlaceholder();
            GridPane grid = scheduleGrid();

            assertTrue(stage.isShowing());
            assertSame(stage.getScene(), grid.getScene());
            assertTrue(placeholder.isFitToWidth());
            assertEquals(List.of("Alice", "Bob"), cardNames(grid));
            assertEquals(8, grid.getColumnConstraints().size());
        });
    }

    // The app opens on the week's lessons; list and find bring up the student table, and week goes back.
    @Test
    public void commands_switchBetweenWeekAndStudentTable() throws Exception {
        runOnFxThread(() -> {
            assertTrue(isShown(schedulePlaceholder()));
            assertFalse(isShown(studentTable()));
            assertActiveNav("#weekNav");

            submitCommand("find Bob");
            assertTrue(isShown(studentTable()));
            assertFalse(isShown(schedulePlaceholder()));

            // Every student is listed again, so the numbers on the calendar match command indices.
            submitCommand("week");
            assertTrue(isShown(schedulePlaceholder()));
            assertFalse(isShown(studentTable()));
            assertEquals(2, model.getFilteredPersonList().size());
            assertEquals(List.of("Alice", "Bob"), cardNames(scheduleGrid()));

            submitCommand("list");
            assertTrue(isShown(studentTable()));
            assertActiveNav("#studentsNav");

            // A rejected command leaves the screen where it was.
            TextField commandField = commandField();
            commandField.setText("delete 99");
            commandField.fireEvent(new ActionEvent());
            assertTrue(isShown(studentTable()));
            assertActiveNav("#studentsNav");

            submitCommand("week");
            assertActiveNav("#weekNav");
        });
    }

    // Alt+Down opens a student's details in the table; on the calendar it must not change the hidden table.
    @Test
    public void altDown_onCalendar_leavesStudentTableUnselected() throws Exception {
        runOnFxThread(() -> {
            commandField().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DOWN,
                    false, false, true, false));
            submitCommand("list");
            ListView<?> table = assertInstanceOf(ListView.class, studentTable());
            assertEquals(-1, table.getSelectionModel().getSelectedIndex());
        });
    }

    @Test
    public void source_filteredAndReplaced_refreshesAttachedSchedule() throws Exception {
        runOnFxThread(() -> {
            GridPane grid = scheduleGrid();
            submitCommand("find Bob");
            assertEquals(List.of("Bob"), cardNames(grid));

            Student bob = model.getFilteredPersonList().get(0);
            model.setPerson(bob, new PersonBuilder(bob).withName("Bobby").build());
            assertTrue(cardNames(grid).isEmpty());

            submitCommand("list");
            assertSame(grid, scheduleGrid());
            assertEquals(List.of("Alice", "Bobby"), cardNames(grid));
        });
    }

    @Test
    public void fillInnerParts_replacement_disposesOldScheduleAndObservesNewOne() throws Exception {
        runOnFxThread(() -> {
            GridPane oldGrid = scheduleGrid();
            List<Node> oldChildren = List.copyOf(oldGrid.getChildren());

            mainWindow.fillInnerParts();
            GridPane newGrid = scheduleGrid();
            assertNotSame(oldGrid, newGrid);
            assertEquals(List.of("Alice", "Bob"), cardNames(newGrid));

            model.addPerson(student("Carol", DayOfWeek.WEDNESDAY));
            assertEquals(oldChildren, oldGrid.getChildren());
            assertEquals(List.of("Alice", "Bob"), cardNames(oldGrid));
            assertEquals(List.of("Alice", "Bob", "Carol"), cardNames(newGrid));
        });
    }

    @Test
    public void closeRequest_windowClosed_disposesScheduleAutomatically() throws Exception {
        runOnFxThread(() -> {
            GridPane grid = scheduleGrid();
            List<Node> children = List.copyOf(grid.getChildren());

            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
            assertFalse(stage.isShowing());

            model.addPerson(student("Carol", DayOfWeek.WEDNESDAY));
            assertEquals(children, grid.getChildren());
            assertEquals(List.of("Alice", "Bob"), cardNames(grid));
        });
    }

    @Test
    public void hide_windowHidden_disposesScheduleAutomatically() throws Exception {
        runOnFxThread(() -> {
            GridPane grid = scheduleGrid();
            List<Node> children = List.copyOf(grid.getChildren());

            stage.hide();
            assertFalse(stage.isShowing());

            model.addPerson(student("Carol", DayOfWeek.WEDNESDAY));
            assertEquals(children, grid.getChildren());
            assertEquals(List.of("Alice", "Bob"), cardNames(grid));
        });
    }

    private ScrollPane schedulePlaceholder() {
        return assertInstanceOf(ScrollPane.class, stage.getScene().lookup("#scheduleScroll"));
    }

    private Node studentTable() {
        return stage.getScene().lookup("#personListView");
    }

    /** Returns true if {@code node} and every container around it are visible. */
    private static boolean isShown(Node node) {
        for (Node current = node; current != null; current = current.getParent()) {
            if (!current.isVisible()) {
                return false;
            }
        }
        return true;
    }

    private GridPane scheduleGrid() {
        return assertInstanceOf(GridPane.class, schedulePlaceholder().getContent());
    }

    private TextField commandField() {
        return assertInstanceOf(TextField.class, stage.getScene().lookup("#commandTextField"));
    }

    private void assertActiveNav(String activeId) {
        for (String id : List.of("#weekNav", "#studentsNav")) {
            boolean active = stage.getScene().lookup(id).getStyleClass().contains("nav-item-active");
            assertEquals(id.equals(activeId), active, id);
        }
    }

    private void submitCommand(String command) {
        TextField commandField = commandField();
        commandField.setText(command);
        commandField.fireEvent(new ActionEvent());
        assertEquals("", commandField.getText(), "Command should execute successfully: " + command);
    }

    private static List<String> cardNames(GridPane grid) {
        return grid.getChildren().stream().filter(Region.class::isInstance).map(Region.class::cast)
                .filter(node -> node.getStyleClass().contains("lesson-card"))
                .map(node -> node.getAccessibleText().split("\n", 2)[0].split(" ", 2)[1]).sorted().toList();
    }

    private static Student student(String name, DayOfWeek day) {
        Lesson lesson = new Lesson(new Subject("Math"),
                new WeeklyLessonSlot(day, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                new HourlyRate(new BigDecimal("50")));
        return new PersonBuilder().withName(name).withLessons(lesson).build();
    }
}
