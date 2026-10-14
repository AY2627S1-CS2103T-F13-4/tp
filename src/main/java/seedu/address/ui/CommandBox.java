package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.IntConsumer;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The UI component that is responsible for receiving user command inputs.
 */
public class CommandBox extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    private static final String FXML = "CommandBox.fxml";
    /** Below this width the key hints would squeeze the placeholder text. */
    private static final double MIN_WIDTH_FOR_HINTS = 900;

    private final CommandExecutor commandExecutor;
    private final CommandHistory history = new CommandHistory();
    private IntConsumer pageScroller = direction -> { };
    private IntConsumer detailsMover = direction -> { };
    private Runnable detailsCloser = () -> { };

    @FXML
    private TextField commandTextField;
    @FXML
    private HBox keyHints;

    /**
     * Creates a {@code CommandBox} with the given {@code CommandExecutor}.
     */
    public CommandBox(CommandExecutor commandExecutor) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        // calls #setStyleToDefault() whenever there is a change to the text of the command box.
        commandTextField.textProperty().addListener((unused1, unused2, unused3) -> setStyleToDefault());
        commandTextField.setStyle(MonospaceFont.style());
        commandTextField.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKey);
        // The hints only take space while the box is empty, so long commands keep the full width,
        // and are left out of narrow windows, where they would crowd the placeholder text.
        keyHints.visibleProperty().bind(commandTextField.textProperty().isEmpty()
                .and(getRoot().widthProperty().greaterThanOrEqualTo(MIN_WIDTH_FOR_HINTS)));
        keyHints.managedProperty().bind(keyHints.visibleProperty());
    }

    /**
     * Sets what Page Up (-1) and Page Down (1) do while typing, so the list can scroll without leaving the box.
     */
    public void setPageScroller(IntConsumer pageScroller) {
        this.pageScroller = requireNonNull(pageScroller);
    }

    /**
     * Sets what Alt+Up (-1) and Alt+Down (1) do, and what Esc does in an empty box, so keyboard users can open
     * and close a student's details without leaving the box.
     */
    public void setDetailsControls(IntConsumer detailsMover, Runnable detailsCloser) {
        this.detailsMover = requireNonNull(detailsMover);
        this.detailsCloser = requireNonNull(detailsCloser);
    }

    /**
     * Runs {@code commandText} as if it had been typed and entered, so it gets the same feedback and history.
     * Anything the user had half-typed is put back afterwards, unless the command failed and is left to fix.
     */
    public void submit(String commandText) {
        String draft = commandTextField.getText();
        commandTextField.setText(commandText);
        handleCommandEntered();
        if (commandTextField.getText().isEmpty()) {
            commandTextField.setText(draft);
        }
        focus();
        commandTextField.end();
    }

    /**
     * Puts {@code commandText} in the box for the user to finish, with the cursor at the end.
     */
    public void prefill(String commandText) {
        commandTextField.setText(commandText);
        focus();
        commandTextField.end();
    }

    /** Returns up to {@code count} of the most recent commands, newest last. */
    public List<String> recent(int count) {
        return history.recent(count);
    }

    /**
     * Moves keyboard focus to the command input.
     */
    public void focus() {
        commandTextField.requestFocus();
    }

    /**
     * Handles the Enter button pressed event.
     */
    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText();
        if (commandText.equals("")) {
            return;
        }

        history.add(commandText);
        try {
            commandExecutor.execute(commandText);
            commandTextField.setText("");
        } catch (CommandException | ParseException e) {
            setStyleToIndicateCommandFailure();
        }
    }

    /**
     * Handles the keyboard shortcuts of the command box: Up and Down for history, Esc to clear (or, in an empty
     * box, to close a student's details), Alt+Up and Alt+Down to open the previous or next student's details,
     * and Page Up and Page Down to scroll the student list.
     */
    private void handleKey(KeyEvent event) {
        KeyCode code = event.getCode();
        if (event.isAltDown() && (code == KeyCode.UP || code == KeyCode.DOWN)) {
            detailsMover.accept(code == KeyCode.UP ? -1 : 1);
        } else if (code == KeyCode.ESCAPE && commandTextField.getText().isEmpty()) {
            detailsCloser.run();
        } else if (code == KeyCode.UP) {
            history.previous(commandTextField.getText()).ifPresent(this::replaceText);
        } else if (code == KeyCode.DOWN) {
            history.next().ifPresent(this::replaceText);
        } else if (code == KeyCode.ESCAPE) {
            commandTextField.clear();
        } else if (code == KeyCode.PAGE_UP) {
            pageScroller.accept(-1);
        } else if (code == KeyCode.PAGE_DOWN) {
            pageScroller.accept(1);
        } else {
            return;
        }
        event.consume();
    }

    private void replaceText(String text) {
        commandTextField.setText(text);
        commandTextField.end();
    }

    /**
     * Sets the command box style to use the default style.
     */
    private void setStyleToDefault() {
        commandTextField.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Sets the command box style to indicate a failed command.
     */
    private void setStyleToIndicateCommandFailure() {
        ObservableList<String> styleClass = commandTextField.getStyleClass();

        if (styleClass.contains(ERROR_STYLE_CLASS)) {
            return;
        }

        styleClass.add(ERROR_STYLE_CLASS);
    }

    /**
     * Represents a function that can execute commands.
     */
    @FunctionalInterface
    public interface CommandExecutor {
        /**
         * Executes the command and returns the result.
         *
         * @see seedu.address.logic.Logic#execute(String)
         */
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }

}
