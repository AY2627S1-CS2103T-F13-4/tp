package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListArchiveCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.WeekCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.HourlyRate;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.ProgressNote;
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
    private boolean failSaves;

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
            JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(dataFile) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    if (failSaves) {
                        throw new IOException("Test save failure");
                    }
                    super.saveAddressBook(addressBook);
                }
            };
            LogicManager logic = new LogicManager(model, new StorageManager(addressBookStorage,
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

            // The week keeps the search; its numbers still match the shown student list.
            submitCommand("week");
            assertTrue(isShown(schedulePlaceholder()));
            assertFalse(isShown(studentTable()));
            assertEquals(1, model.getFilteredPersonList().size());
            assertEquals(List.of("Bob"), cardNames(scheduleGrid()));

            submitCommand("list");
            assertTrue(isShown(studentTable()));
            assertActiveNav("#studentsNav");
            assertEquals(List.of("Alice", "Bob"), model.getFilteredPersonList().stream()
                    .map(student -> student.getName().fullName).toList());

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

    // A search that hides every lesson must not suggest adding one: the way back is list, and the count says so.
    @Test
    public void week_searchHidesLessons_countsThemAndSuggestsList() throws Exception {
        runOnFxThread(() -> {
            submitCommand("find day/sun");
            submitCommand("week");
            assertTrue(stage.getScene().lookup("#emptyState").isVisible());
            assertEquals(WeekPanel.NO_MATCH_TITLE, weekEmptyLabel(".empty-title").getText());
            assertEquals(ListCommand.COMMAND_WORD, weekEmptyLabel(".empty-command").getText());
            assertEquals("Showing 0 of 2 lessons", weekSummary());

            submitCommand("find day/mon");
            assertEquals("Showing 1 of 2 lessons", weekSummary());

            submitCommand("list");
            assertEquals("2 lessons", weekSummary());

            // With no lessons at all, the next step is adding one.
            submitCommand("lesson-delete 1 1");
            submitCommand("lesson-delete 2 1");
            assertEquals(WeekPanel.EMPTY_TITLE, weekEmptyLabel(".empty-title").getText());
            assertEquals(WeekPanel.EMPTY_COMMAND, weekEmptyLabel(".empty-command").getText());
        });
    }

    // The level filters follow the levels in use, primary before secondary, and each one runs find level/LEVEL.
    @Test
    public void levelFilters_followLevelsInUseAndRunFind() throws Exception {
        runOnFxThread(() -> {
            assertFalse(levelNav().isVisible());

            submitCommand("edit 1 level/Sec 3");
            submitCommand("edit 2 level/P5");
            assertFalse(levelNav().isVisible(), "narrow windows leave the tabs room");
        });
        resizeWindow(1000);
        runOnFxThread(() -> {
            assertTrue(levelNav().isVisible());
            assertEquals(List.of("P5", "Sec 3"), levelFilterNames());

            Button secondary = assertInstanceOf(Button.class, levelNav().getChildren().get(2));
            secondary.fire();
            assertTrue(isShown(studentTable()));
            assertActiveNav("#studentsNav");
            assertEquals(List.of("Alice"), model.getFilteredPersonList().stream()
                    .map(student -> student.getName().fullName).toList());

            submitCommand("edit 1 level/P5");
            assertEquals(List.of("P5"), levelFilterNames());
        });
    }

    // Each tab shows the command it runs, so mouse users learn the commands; the hint is fixed text in the FXML,
    // so it must still match the command word, and pressing the tab must do what typing that command does.
    @Test
    public void tabs_showAndRunTheirCommands() throws Exception {
        runOnFxThread(() -> {
            assertEquals(WeekCommand.COMMAND_WORD, navHint("#weekNav"));
            assertEquals(ListCommand.COMMAND_WORD, navHint("#studentsNav"));
            assertEquals(ListArchiveCommand.COMMAND_WORD, navHint("#archiveNav"));
            assertEquals(HelpCommand.COMMAND_WORD, navHint("#helpNav"));

            Button studentsTab = assertInstanceOf(Button.class, stage.getScene().lookup("#studentsNav"));
            studentsTab.fire();
            assertTrue(isShown(studentTable()));
            Button archiveTab = assertInstanceOf(Button.class, stage.getScene().lookup("#archiveNav"));
            archiveTab.fire();
            assertTrue(isShown(archivedStudentTable()));
            assertFalse(isShown(studentTable()));
            assertActiveNav("#archiveNav");
            Button weekTab = assertInstanceOf(Button.class, stage.getScene().lookup("#weekNav"));
            weekTab.fire();
            assertTrue(isShown(schedulePlaceholder()));
            assertFalse(isShown(archivedStudentTable()));
        });
    }

    // Archived students have their own table and indices; viewing or editing them stays on their tab.
    @Test
    public void listArchive_separateTable_usesArchivedIndicesAndKeepsCalendarActive() throws Exception {
        runOnFxThread(() -> {
            submitCommand("add name/Carol phone/91234567 email/carol@example.com address/1");
            submitCommand("archive 1");
            submitCommand("archive 2");
            submitCommand("list-archive");

            ListView<?> archive = archivedStudentTable();
            assertTrue(isShown(archive));
            assertFalse(isShown(studentTable()));
            assertActiveNav("#archiveNav");
            assertEquals(List.of("Alice", "Carol"), tableNames(archive));
            assertEquals(List.of("Bob"), tableNames(studentTable()));
            assertEquals(List.of("Bob"), cardNames(scheduleGrid()));
            assertEquals("1 lesson", weekSummary());

            submitCommand("view 2");
            assertEquals(1, archive.getSelectionModel().getSelectedIndex());
            assertActiveNav("#archiveNav");
            submitCommand("edit 2 name/Caroline");
            assertEquals(List.of("Alice", "Caroline"), tableNames(archive));
            assertEquals(1, archive.getSelectionModel().getSelectedIndex());
            assertActiveNav("#archiveNav");

            submitCommand("find Caroline");
            assertEquals(List.of("Caroline"), tableNames(archive));
            assertActiveNav("#archiveNav");
            assertEquals(List.of("Bob"), cardNames(scheduleGrid()));

            submitCommand("week");
            assertTrue(isShown(schedulePlaceholder()));
            assertActiveNav("#weekNav");
            assertEquals(List.of("Bob"), model.getFilteredPersonList().stream()
                    .map(student -> student.getName().fullName).toList());
            submitCommand("list");
            assertTrue(isShown(studentTable()));
            assertFalse(isShown(archive));
            assertActiveNav("#studentsNav");
        });
    }

    // Saving runs after the model changes its list, so a save error must not leave indices on a hidden table.
    @Test
    public void listSwitch_saveFails_visibleRowsMatchNextCommandIndices() throws Exception {
        runOnFxThread(() -> {
            submitCommand("archive 1");
            submitCommand("list");
            failSaves = true;
            submitFailedCommand("list-archive");
            assertTrue(isShown(archivedStudentTable()));
            assertActiveNav("#archiveNav");
            assertSame(model.getFilteredPersonList(), archivedStudentTable().getItems());
            assertEquals(List.of("Alice"), tableNames(archivedStudentTable()));

            failSaves = false;
            submitCommand("edit 1 name/Alicia");
            assertEquals(List.of("Alicia"), tableNames(archivedStudentTable()));
            assertEquals(List.of("Bob"), tableNames(studentTable()));

            failSaves = true;
            submitFailedCommand("list");
            assertTrue(isShown(studentTable()));
            assertActiveNav("#studentsNav");
            assertSame(model.getFilteredPersonList(), studentTable().getItems());
            assertEquals(List.of("Bob"), tableNames(studentTable()));

            failSaves = false;
            submitCommand("edit 1 name/Bobby");
            assertEquals(List.of("Bobby"), tableNames(studentTable()));
            assertEquals(List.of("Alicia"), tableNames(archivedStudentTable()));
        });
    }

    @Test
    public void weekSwitch_saveFails_showsActiveRowsAndPreservesWeekForOrdinaryErrors() throws Exception {
        runOnFxThread(() -> {
            submitCommand("archive 1");
            submitCommand("list-archive");
            failSaves = true;
            submitFailedCommand("week");
            assertTrue(isShown(studentTable()));
            assertActiveNav("#studentsNav");
            assertSame(model.getFilteredPersonList(), studentTable().getItems());

            failSaves = false;
            submitCommand("week");
            submitFailedCommand("view");
            assertTrue(isShown(schedulePlaceholder()));
            assertActiveNav("#weekNav");
            submitFailedCommand("delete 99");
            assertTrue(isShown(schedulePlaceholder()));
            assertActiveNav("#weekNav");
        });
    }

    // Both lists share the same columns and keyboard controls, while keeping their open details independent.
    @Test
    public void archivedTable_sameLayoutAndKeyboardControlsAsStudents() throws Exception {
        runOnFxThread(() -> {
            submitCommand("archive 1");
            submitCommand("archive 1");
            submitCommand("list-archive");
            ListView<?> archive = archivedStudentTable();
            assertEquals(tableHeadings(studentTable()), tableHeadings(archive));
            assertEquals(studentTable().getStyleClass(), archive.getStyleClass());

            pressKey(KeyCode.ESCAPE, false);
            assertEquals(-1, archive.getSelectionModel().getSelectedIndex());
            pressKey(KeyCode.DOWN, true);
            assertEquals(0, archive.getSelectionModel().getSelectedIndex());
            pressKey(KeyCode.DOWN, true);
            assertEquals(1, archive.getSelectionModel().getSelectedIndex());
            assertEquals(-1, studentTable().getSelectionModel().getSelectedIndex());
        });
    }

    // Level chips come from the selected list, so archived levels do not leak into the active table.
    @Test
    public void levelFilters_selectedArchive_followsArchivedLevels() throws Exception {
        runOnFxThread(() -> {
            submitCommand("edit 1 level/P5");
            submitCommand("edit 2 level/Sec 3");
            submitCommand("archive 1");
            assertEquals(List.of("Sec 3"), levelFilterNames());
            submitCommand("list-archive");
            assertEquals(List.of("P5"), levelFilterNames());
            Button primary = assertInstanceOf(Button.class, levelNav().getChildren().get(1));
            primary.fire();
            assertActiveNav("#archiveNav");
            assertEquals(List.of("Alice"), tableNames(archivedStudentTable()));
            submitCommand("list");
            assertEquals(List.of("Sec 3"), levelFilterNames());
        });
    }

    @Test
    public void tabs_minimumWindowWidth_allCommandsFit() throws Exception {
        resizeWindow(1000);
        resizeWindow(FxTestUtil.callOnFxThread(stage::getMinWidth));
        runOnFxThread(() -> {
            Node navBar = stage.getScene().lookup("#navBar");
            double rightEdge = navBar.localToScene(navBar.getBoundsInLocal()).getMaxX();
            double previousRightEdge = navBar.localToScene(navBar.getBoundsInLocal()).getMinX();
            for (String id : List.of("#weekNav", "#studentsNav", "#archiveNav", "#helpNav")) {
                Button tab = assertInstanceOf(Button.class, stage.getScene().lookup(id));
                double tabLeft = tab.localToScene(tab.getBoundsInLocal()).getMinX();
                double tabRight = tab.localToScene(tab.getBoundsInLocal()).getMaxX();
                assertTrue(tabLeft >= previousRightEdge, id + " should not overlap the previous tab");
                assertTrue(tabRight <= rightEdge, id + " should fit in the navigation bar");
                assertGraphicFits(tab);
                previousRightEdge = tabRight;
            }
        });
    }

    // A filter that does not fully fit is hidden, never shown cut short, and returns when the window widens.
    // Text widths differ between systems, and a window cannot be wider than the screen, which can be narrow on
    // CI machines; so the window widens only by what the hidden filter needs, measured on this system.
    @Test
    public void levelFilters_tooManyToFit_hidesOnlyThoseCutOff() throws Exception {
        runOnFxThread(() -> {
            submitCommand("edit 1 level/P5");
            for (String level : List.of("Secondary 1 Express Stream One", "Secondary 2 Express Stream Two",
                    "JC1")) {
                submitCommand("add name/" + level.substring(level.length() - 3)
                        + " phone/91234567 email/a@b.co address/1 level/" + level);
            }
        });
        resizeWindow(920);
        runOnFxThread(() -> {
            List<Node> filters = levelNav().getChildren().subList(1, levelNav().getChildren().size());
            assertEquals(4, filters.size());
            assertTrue(filters.get(0).isVisible());
            assertFalse(filters.get(3).isVisible());
            filters.stream().map(filter -> assertInstanceOf(Button.class, filter))
                    .forEach(MainWindowTest::assertGraphicFits);
            assertVisibleFiltersFit();
        });
        double widthNeeded = FxTestUtil.callOnFxThread(() -> {
            List<Node> children = levelNav().getChildren();
            double overflow = children.get(children.size() - 1).getBoundsInParent().getMaxX() - levelNav().getWidth();
            return stage.getWidth() + overflow + 10;
        });
        // macOS keeps a window within the screen, which on a 1024px CI runner is narrower than widthNeeded.
        resizeWindow(Math.min(widthNeeded, Screen.getPrimary().getVisualBounds().getWidth()));
        runOnFxThread(() -> {
            String sizes = "window " + stage.getWidth() + " of " + widthNeeded + ", filters " + levelNav().getWidth();
            List<Node> children = levelNav().getChildren();
            for (Node filter : children.subList(1, children.size())) {
                boolean fits = filter.getBoundsInParent().getMaxX() <= levelNav().getWidth();
                assertEquals(fits, filter.isVisible(), sizes);
            }
            if (stage.getWidth() >= widthNeeded) {
                assertTrue(children.stream().allMatch(Node::isVisible), sizes);
            }
        });
    }

    // A short window, such as 1280x720 at 150%, drops the brand and the footer so the screen keeps a few rows.
    @Test
    public void shortWindow_hidesBrandAndFooter() throws Exception {
        resizeWindow(1000, 800);
        runOnFxThread(() -> {
            assertTrue(stage.getScene().lookup("#brand").isVisible());
            assertTrue(stage.getScene().lookup("#statusbarPlaceholder").isVisible());
        });
        resizeWindow(1000, 450);
        runOnFxThread(() -> {
            assertFalse(stage.getScene().lookup("#brand").isVisible());
            assertFalse(stage.getScene().lookup("#statusbarPlaceholder").isVisible());
        });
    }

    // Alt+Down opens a student's details in the table; on the calendar it must not change the hidden table.
    @Test
    public void altDown_onCalendar_leavesStudentTableUnselected() throws Exception {
        runOnFxThread(() -> {
            pressKey(KeyCode.DOWN, true);
            submitCommand("list");
            ListView<?> table = assertInstanceOf(ListView.class, studentTable());
            assertEquals(-1, table.getSelectionModel().getSelectedIndex());
        });
    }

    // view INDEX is the keyboard way to see a student's full details, e.g. from a calendar lesson's "#2".
    @Test
    public void view_index_opensThatStudentInTable() throws Exception {
        runOnFxThread(() -> {
            submitCommand("view 2");
            assertTrue(isShown(studentTable()));
            ListView<?> table = assertInstanceOf(ListView.class, studentTable());
            assertEquals(1, table.getSelectionModel().getSelectedIndex());
        });
    }

    // view INDEX shows whose details these are: a row taller than the list starts at the list's top, whichever
    // row was open before. The list places rows it has not laid out from estimated heights, so this depends on
    // what was viewed before.
    @Test
    public void view_rowTallerThanList_showsItsTopAfterAnyOtherView() throws Exception {
        resizeWindow(1280, 720);
        addStudentsWithNotes();
        for (int before = 1; before <= 6; before++) {
            String viewBefore = "view " + before;
            runOnFxThread(() -> submitCommand(viewBefore));
            Thread.sleep(300);
            runOnFxThread(() -> submitCommand("view 3"));
            assertOpenRowAtListTop(2, "after " + viewBefore);
        }
    }

    // A row already open and taller than the list is lined up by its top too, and Alt+Down to one does the same.
    @Test
    public void view_openTallRowAgainOrAltDown_showsItsTop() throws Exception {
        resizeWindow(760, 400);
        addStudentsWithNotes();
        runOnFxThread(() -> submitCommand("view 9"));
        Thread.sleep(300);
        runOnFxThread(() -> submitCommand("view 10"));
        assertOpenRowAtListTop(9, "view 10 after view 9");
        runOnFxThread(() -> submitCommand("view 10"));
        assertOpenRowAtListTop(9, "view 10 again");
        runOnFxThread(() -> submitCommand("view 8"));
        Thread.sleep(300);
        runOnFxThread(() -> pressKey(KeyCode.DOWN, true));
        assertOpenRowAtListTop(8, "Alt+Down from row 8");
    }

    // Aligning must not overshoot when the list was scrolled away first, leaving the name above the list.
    @Test
    public void view_tallRowAfterScrollingToBottom_showsItsTop() throws Exception {
        resizeWindow(760, 400);
        addStudentsWithNotes();
        runOnFxThread(() -> submitCommand("view 1"));
        Thread.sleep(300);
        for (int page = 0; page < 60; page++) {
            runOnFxThread(() -> mainWindow.getPersonListPanel().scrollPage(1));
        }
        Thread.sleep(100);
        runOnFxThread(() -> submitCommand("view 5"));
        assertOpenRowAtListTop(4, "view 5 after scrolling to the bottom");
    }

    // Alt+Up from a tall row to a shorter one above must show all of that row, not leave its top and name above
    // the list while it grows open. Whether it did depends on the frame the row starts growing in, so this tries
    // each tall row at two sizes.
    @Test
    public void altUp_fromTallRowToRowThatFits_showsWholeRow() throws Exception {
        addStudentsWithNotes();
        for (int height : new int[] {720, 560}) {
            resizeWindow(height == 720 ? 1280 : 1000, height);
            for (int tall : new int[] {5, 3, 9, 5, 3, 9}) {
                String view = "view " + tall;
                runOnFxThread(() -> submitCommand(view));
                Thread.sleep(900);
                runOnFxThread(() -> pressKey(KeyCode.UP, true));
                assertOpenRowFullyShown(tall - 2, "Alt+Up after " + view + " at height " + height);
            }
        }
    }

    // Viewing another student just as the last view's row has finished opening must still show that student's
    // row; the list once ended up showing no rows at all. The timing is narrow, so this tries around it.
    @Test
    public void view_justAfterTallRowOpened_showsNewRow() throws Exception {
        resizeWindow(1280, 720);
        addStudentsWithNotes();
        for (int delay = 225; delay <= 245; delay += 5) {
            runOnFxThread(() -> submitCommand("view 5"));
            Thread.sleep(delay);
            runOnFxThread(() -> submitCommand("view 1"));
            assertOpenRowFullyShown(0, "view 1 " + delay + " ms after view 5");
        }
    }

    // An edit keeps the student's details open, and deleting them closes the details instead of opening the
    // details of whoever takes their row.
    @Test
    public void openStudent_editedOrDeleted_detailsFollowThem() throws Exception {
        runOnFxThread(() -> {
            submitCommand("add name/Carol phone/91234567 email/carol@example.com address/1");
            submitCommand("view 1");
            ListView<?> table = assertInstanceOf(ListView.class, studentTable());

            submitCommand("edit 1 name/Alicia");
            assertEquals(0, table.getSelectionModel().getSelectedIndex());

            submitCommand("delete 1");
            assertEquals(-1, table.getSelectionModel().getSelectedIndex());
        });
    }

    // A school supplied when adding a student must appear in their expanded details.
    @Test
    public void add_schoolPresent_showsSchoolInStudentDetails() throws Exception {
        runOnFxThread(() -> {
            submitCommand("clear");
            submitCommand("add name/Carol phone/91234567 email/carol@example.com address/1"
                    + " sch/Clementi Primary School");
            submitCommand("view 1");

            assertEquals(List.of("Clementi Primary School"), openStudentSchools());
        });
    }

    // School changes rebuild the open row's content, and an absent or cleared school has no section.
    @Test
    public void openStudent_schoolSetReplacedOrCleared_updatesDetails() throws Exception {
        runOnFxThread(() -> {
            submitCommand("view 1");
            assertEquals(List.of(), openStudentSchools());

            submitCommand("edit 1 sch/Clementi Primary School");
            assertEquals(List.of("Clementi Primary School"), openStudentSchools());

            submitCommand("edit 1 sch/Nanyang Primary School");
            assertEquals(List.of("Nanyang Primary School"), openStudentSchools());

            submitCommand("edit 1 sch/");
            assertEquals(List.of(), openStudentSchools());
        });
    }

    // Editing an open student's level moves them to another group. Their details must stay open, not jump to
    // whoever takes their old row.
    @Test
    public void openStudent_movedByEdit_staysOpen() throws Exception {
        runOnFxThread(() -> {
            submitCommand("edit 1 level/P5");
            submitCommand("edit 2 level/P6");
            submitCommand("view 1");
            ListView<?> table = assertInstanceOf(ListView.class, studentTable());

            submitCommand("edit 1 level/JC1");
            assertEquals(List.of("Bob", "Alice"), model.getFilteredPersonList().stream()
                    .map(student -> student.getName().fullName).toList());
            assertEquals(1, table.getSelectionModel().getSelectedIndex());
        });
    }

    // After a usage error, Tab puts the shown example in the box, so the user edits it instead of retyping.
    @Test
    public void tab_afterUsageError_putsExampleInCommandBox() throws Exception {
        runOnFxThread(() -> {
            TextField commandField = commandField();
            commandField.setText("view");
            commandField.fireEvent(new ActionEvent());
            pressKey(KeyCode.TAB, false);
            assertEquals("view 3", commandField.getText());
            // The window's focus owner, not isFocused(), which also needs the OS to focus the test window.
            assertSame(commandField, stage.getScene().getFocusOwner());
        });
    }

    // Tab only uses the example shown now: after a later success, an old error's example must not come back.
    @Test
    public void tab_afterLaterSuccess_doesNothing() throws Exception {
        runOnFxThread(() -> {
            TextField commandField = commandField();
            commandField.setText("view");
            commandField.fireEvent(new ActionEvent());
            submitCommand("list");
            pressKey(KeyCode.TAB, false);
            assertEquals("", commandField.getText());
            // The window's focus owner, not isFocused(), which also needs the OS to focus the test window.
            assertSame(commandField, stage.getScene().getFocusOwner());
        });
    }

    // On Windows and Linux, releasing Alt focuses the menu bar; after Alt+Down the user is still typing.
    @Test
    public void altRelease_afterAltDown_isKeptFromWindow() throws Exception {
        runOnFxThread(() -> {
            boolean[] reachedWindow = {false};
            stage.getScene().addEventHandler(KeyEvent.KEY_RELEASED, event -> reachedWindow[0] = true);

            pressKey(KeyCode.DOWN, true);
            commandField().fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.ALT,
                    false, false, false, false));
            assertFalse(reachedWindow[0]);

            // A release without a shortcut before it is left alone.
            commandField().fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.ALT,
                    false, false, false, false));
            assertTrue(reachedWindow[0]);
        });
    }

    // On the calendar, Alt+Down scrolls, so laptops without Page Down keys can see later hours.
    @Test
    public void altDown_onCalendar_scrollsCalendar() throws Exception {
        runOnFxThread(() -> {
            stage.setWidth(1000);
            stage.setHeight(500);
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
            ScrollPane calendar = schedulePlaceholder();
            calendar.setVvalue(0);
            pressKey(KeyCode.DOWN, true);
            assertTrue(calendar.getVvalue() > 0);
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

    private HBox levelNav() {
        return assertInstanceOf(HBox.class, stage.getScene().lookup("#levelNav"));
    }

    /**
     * Resizes the window and waits for its content to follow, as the window system resizes it after the
     * request returns. A screen narrower than {@code width} caps the window, so this waits for any change.
     */
    private void resizeWindow(double width) throws Exception {
        resizeWindow(width, Double.NaN);
    }

    /** Resizes the window as {@link #resizeWindow(double)} does, also setting its height unless it is NaN. */
    private void resizeWindow(double width, double height) throws Exception {
        List<Double> before = FxTestUtil.callOnFxThread(() -> {
            List<Double> sceneSize = List.of(stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setWidth(width);
            if (!Double.isNaN(height)) {
                stage.setHeight(height);
            }
            return sceneSize;
        });
        for (int attempt = 0; attempt < 100; attempt++) {
            boolean resized = FxTestUtil.callOnFxThread(() -> !List.of(stage.getScene().getWidth(),
                    stage.getScene().getHeight()).equals(before));
            if (resized) {
                runOnFxThread(() -> stage.getScene().getRoot().layout());
                return;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("The window did not resize to " + width);
    }

    private void assertVisibleFiltersFit() {
        for (Node filter : levelNav().getChildren()) {
            if (filter.isVisible()) {
                assertTrue(filter.getBoundsInParent().getMaxX() <= levelNav().getWidth());
            }
        }
    }

    private static void assertGraphicFits(Button button) {
        Node graphic = button.getGraphic();
        assertTrue(graphic.localToScene(graphic.getBoundsInLocal()).getMinX()
                >= button.localToScene(button.getBoundsInLocal()).getMinX());
        assertTrue(graphic.localToScene(graphic.getBoundsInLocal()).getMaxX()
                <= button.localToScene(button.getBoundsInLocal()).getMaxX());
    }

    private List<String> levelFilterNames() {
        // Read from each filter's graphic, as a filter added since the last layout has no skin to look up yet
        return levelNav().getChildren().stream().skip(1)
                .map(filter -> ((HBox) ((Button) filter).getGraphic()).getChildren().stream()
                        .filter(Label.class::isInstance).map(label -> ((Label) label).getText()).findFirst().get())
                .toList();
    }

    private String weekSummary() {
        return assertInstanceOf(Label.class, stage.getScene().lookup("#weekSummary")).getText();
    }

    /** Returns a label of the week's empty state; the student table has its own, with the same style classes. */
    private Label weekEmptyLabel(String selector) {
        return assertInstanceOf(Label.class, stage.getScene().lookup("#emptyState").lookup(selector));
    }

    private ListView<?> studentTable() {
        return assertInstanceOf(ListView.class, mainWindow.getPersonListPanel().getRoot().lookup("#personListView"));
    }

    private ListView<?> archivedStudentTable() {
        return stage.getScene().getRoot().lookupAll("#personListView").stream().filter(node -> node != studentTable())
                .map(node -> assertInstanceOf(ListView.class, node)).findFirst().orElseThrow();
    }

    private static List<String> tableNames(ListView<?> table) {
        return table.getItems().stream().map(Student.class::cast).map(student -> student.getName().fullName).toList();
    }

    private static List<String> tableHeadings(ListView<?> table) {
        return table.getParent().lookupAll(".table-heading").stream().map(Label.class::cast).map(Label::getText)
                .sorted().toList();
    }

    /** Reads school sections from the open row after the list has laid out any replacement cells. */
    private List<String> openStudentSchools() {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        FlowPane details = assertInstanceOf(FlowPane.class, studentTable().lookup(".student-row-open #details"));
        assertTrue(isShown(details));
        assertTrue(details.isManaged());
        return details.lookupAll(".detail-caption").stream().filter(Label.class::isInstance).map(Label.class::cast)
                .filter(label -> label.getText().equals("School"))
                .flatMap(label -> label.getParent().getChildrenUnmodifiable().stream())
                .filter(Text.class::isInstance).map(Text.class::cast).map(Text::getText).toList();
    }

    /**
     * Adds eight students after Alice and Bob, three of whose details are taller than a short list and one far
     * taller, then shows the table.
     */
    private void addStudentsWithNotes() throws Exception {
        int[] noteCounts = {20, 0, 20, 0, 0, 0, 200, 20};
        String[] names = {"Carol", "Dan", "Eve", "Fay", "Gus", "Hal", "Ivy", "Jack"};
        runOnFxThread(() -> {
            for (int i = 0; i < names.length; i++) {
                ProgressNote[] notes = new ProgressNote[noteCounts[i]];
                for (int n = 0; n < notes.length; n++) {
                    notes[n] = new ProgressNote(LocalDate.of(2026, 1, 1).plusDays(n), "Fractions");
                }
                model.addPerson(new PersonBuilder(student(names[i], DayOfWeek.WEDNESDAY)).withNotes(notes).build());
            }
            submitCommand("list");
        });
        Thread.sleep(600);
    }

    /**
     * Waits well past the 220 ms open animation, then checks that row {@code index} is open, taller than the list
     * and starts at the list's top.
     */
    private void assertOpenRowAtListTop(int index, String message) throws Exception {
        Thread.sleep(450);
        runOnFxThread(() -> {
            assertEquals(index, studentTable().getSelectionModel().getSelectedIndex(), message);
            VirtualFlow<?> flow = assertInstanceOf(VirtualFlow.class, studentTable().lookup(".virtual-flow"));
            IndexedCell<?> row = flow.getVisibleCell(index);
            assertTrue(row != null && row.isVisible(), message + ": the open row should be in view");
            Bounds flowBounds = flow.localToScene(flow.getLayoutBounds());
            Bounds rowBounds = row.localToScene(row.getLayoutBounds());
            assertTrue(rowBounds.getHeight() > flowBounds.getHeight(), message + ": the row should be taller");
            assertEquals(0, rowBounds.getMinY() - flowBounds.getMinY(), 1, message);
        });
    }

    /** Waits past the open animation, then checks that row {@code index} is open and wholly inside the list. */
    private void assertOpenRowFullyShown(int index, String message) throws Exception {
        Thread.sleep(700);
        runOnFxThread(() -> {
            assertEquals(index, studentTable().getSelectionModel().getSelectedIndex(), message);
            VirtualFlow<?> flow = assertInstanceOf(VirtualFlow.class, studentTable().lookup(".virtual-flow"));
            IndexedCell<?> row = flow.getVisibleCell(index);
            assertTrue(row != null && row.isVisible(), message + ": the open row should be in view");
            Bounds flowBounds = flow.localToScene(flow.getLayoutBounds());
            Bounds rowBounds = row.localToScene(row.getLayoutBounds());
            assertTrue(rowBounds.getMinY() > flowBounds.getMinY() - 1, message + ": top at "
                    + (rowBounds.getMinY() - flowBounds.getMinY()));
            assertTrue(rowBounds.getMaxY() < flowBounds.getMaxY() + 1, message + ": bottom below the list");
        });
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

    private void pressKey(KeyCode code, boolean alt) {
        commandField().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, alt, false));
    }

    /** Returns a tab's command hint, checking it is in the command font like the command box. */
    private String navHint(String tabId) {
        Label hint = (Label) stage.getScene().lookup(tabId).lookup(".nav-hint");
        assertEquals(MonospaceFont.style(), hint.getStyle(), tabId);
        return hint.getText();
    }

    private void assertActiveNav(String activeId) {
        for (String id : List.of("#weekNav", "#studentsNav", "#archiveNav")) {
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

    private void submitFailedCommand(String command) {
        TextField commandField = commandField();
        commandField.setText(command);
        commandField.fireEvent(new ActionEvent());
        assertEquals(command, commandField.getText(), "A rejected command should remain available to correct");
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
