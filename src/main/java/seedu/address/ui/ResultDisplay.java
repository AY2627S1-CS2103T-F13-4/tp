package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 * The row is neutral before the first command, then marked as a success or an error.
 */
public class ResultDisplay extends UiPart<Region> {

    static final String WELCOME_MESSAGE = "Type a command below and press Enter. "
            + "Try list, find NAME, or help (F1) for all commands.";

    private static final String FXML = "ResultDisplay.fxml";
    private static final String SUCCESS_STYLE_CLASS = "result-success";
    private static final String ERROR_STYLE_CLASS = "result-error";
    /** Heights fitting two and four lines of text. Multi-line messages, such as command usage, get four. */
    private static final double SHORT_HEIGHT = 36;
    private static final double TALL_HEIGHT = 72;

    @FXML
    private HBox resultRow;
    @FXML
    private Label resultIcon;
    @FXML
    private TextArea resultDisplay;

    /**
     * Creates a {@code ResultDisplay} showing a short hint on how to start.
     */
    public ResultDisplay() {
        super(FXML);
        resultIcon.setText("i");
        resultDisplay.setText(WELCOME_MESSAGE);
    }

    /**
     * Shows the feedback of a command that ran successfully.
     */
    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        show(feedbackToUser, SUCCESS_STYLE_CLASS, "✓");
    }

    /**
     * Shows why a command could not run.
     */
    public void setErrorToUser(String errorMessage) {
        requireNonNull(errorMessage);
        show(errorMessage, ERROR_STYLE_CLASS, "!");
    }

    private void show(String message, String styleClass, String icon) {
        resultRow.getStyleClass().removeAll(SUCCESS_STYLE_CLASS, ERROR_STYLE_CLASS);
        resultRow.getStyleClass().add(styleClass);
        resultIcon.setText(icon);
        resultDisplay.setText(message);
        resultDisplay.setPrefHeight(message.contains("\n") ? TALL_HEIGHT : SHORT_HEIGHT);
        resultDisplay.positionCaret(0);
    }
}
