package seedu.address.ui;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LEVEL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListArchiveCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.WeekCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL = "https://ay2627s1-cs2103t-f13-4.github.io/tp/UserGuide.html";
    private static final String FXML = "MainWindow.fxml";
    private static final Logger logger = LogsCenter.getLogger(MainWindow.class);
    /** Index of the first level filter; the divider before them is child 0 of their row. */
    private static final int LEVEL_NAV_FIRST_FILTER = 1;
    /** Below this height the brand and the footer are hidden, leaving the screen room for a few rows. */
    private static final double SHORT_WINDOW_HEIGHT = 560;
    /** Below this width the level filters are hidden, leaving the tabs room. */
    private static final double LEVEL_NAV_MIN_WIDTH = 900;
    /** Number of recent commands shown as chips beside a successful result. */
    private static final int RECENT_COMMANDS = 3;
    private static final String NAV_ACTIVE_STYLE_CLASS = "nav-item-active";
    private static final String COMMAND_PANEL_FOCUSED_STYLE_CLASS = "command-panel-focused";
    /** Moves the cursor back to the command box: Cmd+L on macOS, Ctrl+L elsewhere. */
    private static final KeyCombination FOCUS_COMMAND_BOX = new KeyCodeCombination(KeyCode.L,
            KeyCombination.SHORTCUT_DOWN);
    /** Filled into the command box by the Add student button. */
    private static final String ADD_TEMPLATE = AddCommand.COMMAND_WORD + " " + PREFIX_NAME + " " + PREFIX_PHONE
            + " " + PREFIX_EMAIL + " " + PREFIX_ADDRESS;


    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private PersonListPanel archivedPersonListPanel;
    private WeekPanel weekPanel;
    private ResultDisplay resultDisplay;
    private CommandBox commandBox;
    private final Consumer<String> openDocument;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    @FXML
    private MenuBar menuBar;

    @FXML
    private VBox commandPanel;

    @FXML
    private Button weekNav;

    @FXML
    private Button studentsNav;

    @FXML
    private Button archiveNav;

    @FXML
    private HBox brand;

    @FXML
    private HBox navBar;

    @FXML
    private HBox levelNav;

    @FXML
    private Button helpNav;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath, Consumer<String> openDocument) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;
        this.openDocument = openDocument;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());
        // On macOS the menus belong in the system menu bar; elsewhere this has no effect.
        menuBar.setUseSystemMenuBar(true);

        setAccelerators();

        primaryStage.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> disposeWeekPanel());
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /** Shows {@code message} as a warning in the result display, e.g. a problem found at startup. */
    void showWarning(String message) {
        resultDisplay.setWarningToUser(message);
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in a later version of the SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField, some
         * accelerators (e.g., F1) will not work when the focus is in it because the
         * key event is consumed by the TextInputControl.
         *
         * For now, we add the following event filter to capture such key events and open
         * the user guide so as to support accelerators even when focus is
         * in CommandBox.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        personListPanel = new PersonListPanel(logic.getFilteredActivePersonList(),
                logic.getAddressBook().getPersonList(), false, logic.getRateSortOrder());
        archivedPersonListPanel = new PersonListPanel(logic.getFilteredArchivedPersonList(),
                logic.getAddressBook().getArchivedPersonList(), true, logic.getRateSortOrder());
        personListPanelPlaceholder.getChildren().setAll(personListPanel.getRoot(), archivedPersonListPanel.getRoot());

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        disposeWeekPanel();
        weekPanel = new WeekPanel(logic.getFilteredActivePersonList(), logic.getAddressBook().getPersonList(),
                logic.getLessonPredicate());
        personListPanelPlaceholder.getChildren().add(weekPanel.getRoot());
        // Only the shown screen takes part in layout, so a hidden one can never make the shown one wider than
        // the window.
        personListPanelPlaceholder.getChildren()
                .forEach(screen -> screen.managedProperty().bind(screen.visibleProperty()));

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        commandBox = new CommandBox(this::executeCommand);
        commandBox.setPageScroller(direction -> {
            if (weekPanel.getRoot().isVisible()) {
                weekPanel.scrollPage(direction);
            } else {
                visiblePersonListPanel().scrollPage(direction);
            }
        });
        // Alt+Up/Down step through students' details on the table and scroll the calendar, so neither screen
        // needs Page Up or Page Down, which many laptops lack. Esc closes details only on the table.
        commandBox.setDetailsControls(direction -> {
            if (weekPanel.getRoot().isVisible()) {
                weekPanel.scrollPage(direction);
            } else {
                visiblePersonListPanel().moveDetails(direction);
            }
        }, () -> {
            if (!weekPanel.getRoot().isVisible()) {
                visiblePersonListPanel().closeDetails();
            }
        });
        commandBox.setSideScroller(direction -> {
            if (weekPanel.getRoot().isVisible()) {
                weekPanel.scrollSideways(direction);
            }
        });
        commandBox.setExampleSupplier(resultDisplay::getExample);
        // The command panel shows an accent outline while the cursor is in the command box.
        commandBox.focusedProperty().addListener((unused, wasFocused, focused) -> {
            commandPanel.getStyleClass().remove(COMMAND_PANEL_FOCUSED_STYLE_CLASS);
            if (focused) {
                commandPanel.getStyleClass().add(COMMAND_PANEL_FOCUSED_STYLE_CLASS);
            }
        });
        // Clicking a row or tab takes the cursor out of the command box; the shortcut brings it back from anywhere.
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (FOCUS_COMMAND_BOX.match(event)) {
                commandBox.focus();
                event.consume();
            }
        });
        personListPanel.setOnAddStudent(() -> commandBox.prefill(ADD_TEMPLATE));
        archivedPersonListPanel.setOnAddStudent(() -> commandBox.prefill(ADD_TEMPLATE));
        resultDisplay.setOnRecentPicked(commandBox::prefill);
        fillNavigation();
        Region content = (Region) brand.getParent();
        BooleanBinding tall = content.heightProperty().greaterThanOrEqualTo(SHORT_WINDOW_HEIGHT);
        for (Node node : List.of(brand, statusbarPlaceholder)) {
            node.visibleProperty().bind(tall);
            node.managedProperty().bind(tall);
        }
        // The app opens on the week's lessons; list and find bring up the student table.
        showScreen(CommandResult.Screen.WEEK);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
        // The list now comes before the command box, so focus the command box explicitly for typing.
        commandBox.focus();
    }

    /**
     * Fills the navigation under the command bar. Each tab shows the command it runs, so mouse users learn
     * the commands too. The level filters list the schooling levels in use and are rebuilt whenever the
     * students change.
     */
    private void fillNavigation() {
        for (Button tab : List.of(weekNav, studentsNav, archiveNav, helpNav)) {
            tab.getGraphic().lookup(".nav-hint").setStyle(MonospaceFont.style());
        }
        for (ObservableList<Student> students : List.of(logic.getAddressBook().getPersonList(),
                logic.getAddressBook().getArchivedPersonList())) {
            students.addListener((ListChangeListener<Student>) change -> fillLevelNav());
        }
        fillLevelNav();
        levelNav.widthProperty().addListener((unused, oldWidth, newWidth) -> hideCutOffFilters());
        levelNav.visibleProperty().bind(Bindings.size(levelNav.getChildren()).greaterThan(LEVEL_NAV_FIRST_FILTER)
                .and(navBar.widthProperty().greaterThanOrEqualTo(LEVEL_NAV_MIN_WIDTH)));
        levelNav.managedProperty().bind(levelNav.visibleProperty());
    }

    private void fillLevelNav() {
        List<Student> students = logic.isArchiveView() ? logic.getAddressBook().getArchivedPersonList()
                : logic.getAddressBook().getPersonList();
        levelNav.getChildren().remove(LEVEL_NAV_FIRST_FILTER, levelNav.getChildren().size());
        students.stream()
                .map(Student::getSchoolingLevel).flatMap(Optional::stream).map(Object::toString)
                .distinct()
                .sorted(Comparator.comparing(SchoolingLevel::stageOf).thenComparing(String.CASE_INSENSITIVE_ORDER))
                .forEach(level -> {
                    Button item = new Button();
                    // Hide a whole chip when it does not fit, rather than shrinking its label first.
                    item.setMinWidth(Region.USE_PREF_SIZE);
                    item.getStyleClass().addAll("nav-item", "level-chip");
                    String command = FindCommand.COMMAND_WORD + " " + PREFIX_LEVEL + level;
                    // Chips stay short, so the command they run shows on hover instead
                    levelChip(item, PersonCard.stageStyleClass(level), level);
                    item.setTooltip(new Tooltip(command));
                    item.setOnAction(event -> commandBox.submit(command));
                    item.boundsInParentProperty().addListener((unused, oldBounds, newBounds) -> hideCutOffFilters());
                    levelNav.getChildren().add(item);
                });
    }

    /**
     * Hides the level filters that do not fully fit, so a book with many levels never shows half a level,
     * such as "Secondary" for "Secondary 1 Express", or covers "All commands".
     */
    private void hideCutOffFilters() {
        List<Node> filters = levelNav.getChildren();
        for (Node filter : filters.subList(LEVEL_NAV_FIRST_FILTER, filters.size())) {
            filter.setVisible(filter.getBoundsInParent().getMaxX() <= levelNav.getWidth());
        }
    }

    /** Lays out a level filter: a dot in its stage's colour and the level. */
    private static void levelChip(Button item, String dotStyleClass, String label) {
        Region dot = new Region();
        dot.getStyleClass().addAll("nav-dot", dotStyleClass);
        Label name = new Label(label);
        name.getStyleClass().add("nav-label");
        name.setMinWidth(Region.USE_PREF_SIZE);
        HBox content = new HBox(8, dot, name);
        content.setAlignment(Pos.CENTER_LEFT);
        item.setGraphic(content);
        item.setFocusTraversable(false);
    }

    @FXML
    private void handleListStudents() {
        commandBox.submit(ListCommand.COMMAND_WORD);
    }

    @FXML
    private void handleListArchive() {
        commandBox.submit(ListArchiveCommand.COMMAND_WORD);
    }

    @FXML
    private void handleShowWeek() {
        commandBox.submit(WeekCommand.COMMAND_WORD);
    }

    /** Shows the week's lessons or the selected student table, and marks the matching tab. */
    private void showScreen(CommandResult.Screen screen) {
        if (screen == CommandResult.Screen.UNCHANGED) {
            return;
        }
        boolean week = screen == CommandResult.Screen.WEEK;
        boolean archive = !week && logic.isArchiveView();
        weekPanel.getRoot().setVisible(week);
        personListPanel.getRoot().setVisible(!week && !archive);
        archivedPersonListPanel.getRoot().setVisible(archive);
        setActive(weekNav, week);
        setActive(studentsNav, !week && !archive);
        setActive(archiveNav, archive);
        fillLevelNav();
        // On the calendar, Alt+Left/Right also scroll sideways.
        commandBox.setAltHint(week ? "Alt ↑↓←→" : "Alt ↑↓", week ? "Scroll" : "Details");
        if (week) {
            // The calendar is out of layout while hidden, and the scroll to the current time can run before the
            // next layout pass, so size it now, with the styles of everything changed above. Before the window
            // first shows there is nothing to do: showing it lays everything out for the screen it is on.
            if (primaryStage.isShowing()) {
                Parent root = primaryStage.getScene().getRoot();
                root.applyCss();
                root.layout();
            }
            weekPanel.scrollToCurrentTime();
        }
    }

    private static void setActive(Button navItem, boolean active) {
        navItem.getStyleClass().remove(NAV_ACTIVE_STYLE_CLASS);
        if (active) {
            navItem.getStyleClass().add(NAV_ACTIVE_STYLE_CLASS);
        }
    }

    private void disposeWeekPanel() {
        if (weekPanel != null) {
            weekPanel.dispose();
        }
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        // A saved size can be larger than the screen the window opens on, e.g. a 1280x720 display at 150%.
        Rectangle2D screen = screenFor(guiSettings).getVisualBounds();
        primaryStage.setHeight(Math.min(guiSettings.getWindowHeight(), screen.getHeight()));
        primaryStage.setWidth(Math.min(guiSettings.getWindowWidth(), screen.getWidth()));
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    /**
     * Returns the screen containing the saved window position, or the primary screen if there is none.
     */
    private static Screen screenFor(GuiSettings guiSettings) {
        if (guiSettings.getWindowCoordinates() == null) {
            return Screen.getPrimary();
        }
        return Screen.getScreensForRectangle(guiSettings.getWindowCoordinates().getX(),
                        guiSettings.getWindowCoordinates().getY(), 1, 1)
                .stream().findFirst().orElse(Screen.getPrimary());
    }

    /**
     * Opens the user guide in the default browser.
     */
    @FXML
    public void handleHelp() {
        try {
            openDocument.accept(USERGUIDE_URL);
        } catch (RuntimeException e) {
            logger.warning("Could not open user guide: " + e);
            showWarning("Could not open the browser. Open the user guide manually: " + USERGUIDE_URL);
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    private PersonListPanel visiblePersonListPanel() {
        return archivedPersonListPanel.getRoot().isVisible() ? archivedPersonListPanel : personListPanel;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        Student activeDetails = personListPanel.getOpenStudent();
        Student archivedDetails = archivedPersonListPanel.getOpenStudent();
        try {
            CommandResult commandResult = logic.execute(commandText);
            resultDisplay.setRecent(commandBox.recent(RECENT_COMMANDS));
            if (commandResult.isWarning()) {
                resultDisplay.setWarningToUser(commandResult.getFeedbackToUser());
            } else {
                resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());
            }
            showScreen(commandResult.getScreen());
            commandResult.getStudentToOpen().ifPresent(index ->
                    visiblePersonListPanel().openStudent(index.getZeroBased()));

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            personListPanel.restoreOpenStudent(activeDetails);
            archivedPersonListPanel.restoreOpenStudent(archivedDetails);
            // Failed saves restore the model's list selection; keep the screen that was visible before the command.
            resultDisplay.setErrorToUser(e.getMessage());
            throw e;
        }
    }
}
