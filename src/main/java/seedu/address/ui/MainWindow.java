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
import java.util.logging.Logger;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
import seedu.address.logic.commands.HelpCommand;
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

    private static final String FXML = "MainWindow.fxml";
    /** Below this window width the sidebar is hidden, leaving the table its columns. */
    private static final double SIDEBAR_MIN_WINDOW_WIDTH = 1100;
    /** Number of recent commands shown as chips beside a successful result. */
    private static final int RECENT_COMMANDS = 3;
    private static final String NAV_ACTIVE_STYLE_CLASS = "nav-item-active";
    /** Filled into the command box by the Add student button. */
    private static final String ADD_TEMPLATE = AddCommand.COMMAND_WORD + " " + PREFIX_NAME + " " + PREFIX_PHONE
            + " " + PREFIX_EMAIL + " " + PREFIX_ADDRESS;

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private WeekPanel weekPanel;
    private ResultDisplay resultDisplay;
    private CommandBox commandBox;
    private HelpWindow helpWindow;

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
    private VBox sidebar;

    @FXML
    private Button weekNav;

    @FXML
    private Button studentsNav;

    @FXML
    private VBox levelNav;

    @FXML
    private Label levelCaption;

    @FXML
    private Button helpNav;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());
        // On macOS the menus belong in the system menu bar; elsewhere this has no effect.
        menuBar.setUseSystemMenuBar(true);

        setAccelerators();

        helpWindow = new HelpWindow();
        primaryStage.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> disposeWeekPanel());
    }

    public Stage getPrimaryStage() {
        return primaryStage;
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
         * the help window purposely so as to support accelerators even when focus is
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
        personListPanel = new PersonListPanel(logic.getFilteredPersonList(), logic.getAddressBook().getPersonList());
        personListPanelPlaceholder.getChildren().setAll(personListPanel.getRoot());

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        disposeWeekPanel();
        weekPanel = new WeekPanel(logic.getFilteredPersonList());
        personListPanelPlaceholder.getChildren().add(weekPanel.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        commandBox = new CommandBox(this::executeCommand);
        commandBox.setPageScroller(direction -> {
            if (weekPanel.getRoot().isVisible()) {
                weekPanel.scrollPage(direction);
            } else {
                personListPanel.scrollPage(direction);
            }
        });
        // Details belong to the student table, so the keys do nothing while the calendar shows.
        commandBox.setDetailsControls(direction -> {
            if (!weekPanel.getRoot().isVisible()) {
                personListPanel.moveDetails(direction);
            }
        }, () -> {
            if (!weekPanel.getRoot().isVisible()) {
                personListPanel.closeDetails();
            }
        });
        personListPanel.setOnAddStudent(() -> commandBox.prefill(ADD_TEMPLATE));
        resultDisplay.setOnRecentPicked(commandBox::prefill);
        fillSidebar();
        // The app opens on the week's lessons; list and find bring up the student table.
        showScreen(CommandResult.Screen.WEEK);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
        // The list now comes before the command box, so focus the command box explicitly for typing.
        commandBox.focus();
    }

    /**
     * Fills the sidebar. Each item shows the command it runs, so mouse users learn the commands too.
     * The level items list the schooling levels in use and are rebuilt whenever the students change.
     */
    private void fillSidebar() {
        navItem(weekNav, null, "This week", WeekCommand.COMMAND_WORD);
        navItem(studentsNav, null, "Students", ListCommand.COMMAND_WORD);
        navItem(helpNav, null, "All commands", HelpCommand.COMMAND_WORD);
        ObservableList<Student> students = logic.getAddressBook().getPersonList();
        students.addListener((ListChangeListener<Student>) change -> fillLevelNav(students));
        fillLevelNav(students);
        Region content = (Region) sidebar.getParent();
        sidebar.visibleProperty().bind(content.widthProperty().greaterThanOrEqualTo(SIDEBAR_MIN_WINDOW_WIDTH));
        sidebar.managedProperty().bind(sidebar.visibleProperty());
    }

    private void fillLevelNav(List<Student> students) {
        levelNav.getChildren().clear();
        students.stream()
                .map(Student::getSchoolingLevel).flatMap(Optional::stream).map(Object::toString)
                .distinct()
                .sorted(Comparator.comparing(SchoolingLevel::stageOf).thenComparing(String.CASE_INSENSITIVE_ORDER))
                .forEach(level -> {
                    Button item = new Button();
                    item.getStyleClass().add("nav-item");
                    item.setMaxWidth(Double.MAX_VALUE);
                    String command = FindCommand.COMMAND_WORD + " " + PREFIX_LEVEL + level;
                    navItem(item, PersonCard.stageStyleClass(level), level, command);
                    item.setOnAction(event -> commandBox.submit(command));
                    levelNav.getChildren().add(item);
                });
        // An empty book has no levels, so hide the caption rather than show a heading with nothing under it.
        levelCaption.setVisible(!levelNav.getChildren().isEmpty());
        levelCaption.setManaged(levelCaption.isVisible());
    }

    /** Lays out a sidebar item: an optional coloured dot, its label, and the command it runs. */
    private static void navItem(Button item, String dotStyleClass, String label, String command) {
        HBox content = new HBox(8);
        content.setAlignment(Pos.CENTER_LEFT);
        if (dotStyleClass != null) {
            Region dot = new Region();
            dot.getStyleClass().addAll("nav-dot", dotStyleClass);
            content.getChildren().add(dot);
        }
        Label name = new Label(label);
        name.getStyleClass().add("nav-label");
        name.setMinWidth(Region.USE_PREF_SIZE);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label hint = new Label(command);
        hint.getStyleClass().add("nav-hint");
        hint.setStyle(MonospaceFont.style());
        content.getChildren().addAll(name, spacer, hint);
        item.setGraphic(content);
        item.setText(null);
        item.setFocusTraversable(false);
    }

    @FXML
    private void handleListStudents() {
        commandBox.submit(ListCommand.COMMAND_WORD);
    }

    @FXML
    private void handleShowWeek() {
        commandBox.submit(WeekCommand.COMMAND_WORD);
    }

    /** Shows the week's lessons or the student table, and marks the matching sidebar item. */
    private void showScreen(CommandResult.Screen screen) {
        if (screen == CommandResult.Screen.UNCHANGED) {
            return;
        }
        boolean week = screen == CommandResult.Screen.WEEK;
        weekPanel.getRoot().setVisible(week);
        personListPanel.getRoot().setVisible(!week);
        setActive(weekNav, week);
        setActive(studentsNav, !week);
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
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
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
        helpWindow.hide();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setRecent(commandBox.recent(RECENT_COMMANDS));
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());
            showScreen(commandResult.getScreen());

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setErrorToUser(e.getMessage());
            throw e;
        }
    }
}
