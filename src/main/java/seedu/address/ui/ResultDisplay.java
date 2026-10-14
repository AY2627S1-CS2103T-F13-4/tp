package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Consumer;

import javafx.beans.binding.DoubleExpression;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

/**
 * A UI component that displays the result of a command execution.
 * The row is neutral before the first command, then marked as a success or an error.
 * Command formats and examples in the message are labelled and shown in a monospace font.
 */
public class ResultDisplay extends UiPart<Region> {

    static final String WELCOME_MESSAGE = "Type a command below and press Enter. "
            + "Try list, find NAME, or help (F1) for all commands.";

    private static final String FXML = "ResultDisplay.fxml";
    private static final String SUCCESS_STYLE_CLASS = "result-success";
    private static final String ERROR_STYLE_CLASS = "result-error";
    /** Width of the "Format" and "Example" captions, plus the gap after them. */
    private static final double CAPTION_WIDTH = 64;
    /** Room for the scroll pane's vertical bar, so wrapped text never sits under it. */
    private static final double SCROLL_BAR_ALLOWANCE = 14;
    /** Longest success message shown beside the recent commands; longer ones need the row's full width. */
    private static final int MAX_RECENT_ROW_MESSAGE = 60;

    @FXML
    private HBox resultRow;
    @FXML
    private Label resultIcon;
    @FXML
    private ScrollPane resultScroll;
    @FXML
    private VBox resultContent;

    private List<String> recent = List.of();
    private Consumer<String> onRecentPicked = command -> { };

    /**
     * Creates a {@code ResultDisplay} showing a short hint on how to start.
     */
    public ResultDisplay() {
        super(FXML);
        resultIcon.setText("i");
        render(WELCOME_MESSAGE);
    }

    /**
     * Shows the feedback of a command that ran successfully.
     */
    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        show(feedbackToUser, SUCCESS_STYLE_CLASS, "");
    }

    /**
     * Sets the recent commands shown as chips beside a one-line success, as in "Recent  list  find alex".
     */
    public void setRecent(List<String> recent) {
        this.recent = List.copyOf(recent);
    }

    /** Sets what clicking a recent command does, e.g. put it back in the command box. */
    public void setOnRecentPicked(Consumer<String> onRecentPicked) {
        this.onRecentPicked = onRecentPicked;
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
        boolean success = icon.isEmpty();
        Motion.fadeIn(resultContent, 0.2);
        if (!success) {
            Motion.shake(resultRow);
        }
        if (success && !recent.isEmpty() && message.length() <= MAX_RECENT_ROW_MESSAGE
                && FeedbackLines.split(message).size() == 1) {
            // A short one-line success reads like the mockup: recent commands on the left, the result on the right.
            resultIcon.setVisible(false);
            resultIcon.setManaged(false);
            resultContent.getChildren().setAll(recentRow(message));
            return;
        }
        resultIcon.setVisible(true);
        resultIcon.setManaged(true);
        // Drawn rather than typed, since not every system font has a check mark character.
        resultIcon.setGraphic(success ? checkMark() : null);
        render(message);
    }

    private Node recentRow(String message) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label caption = new Label("Recent");
        caption.getStyleClass().add("result-caption");
        row.getChildren().add(caption);
        for (String command : recent) {
            Label chip = new Label(command);
            chip.getStyleClass().add("recent-chip");
            chip.setStyle(MonospaceFont.style());
            chip.setCursor(Cursor.HAND);
            chip.setOnMouseClicked(event -> onRecentPicked.accept(command));
            row.getChildren().add(chip);
        }
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Text result = new Text(message);
        result.getStyleClass().add("result-text");
        HBox outcome = new HBox(6, checkMark(), result);
        outcome.setAlignment(Pos.CENTER_RIGHT);
        row.getChildren().addAll(spacer, outcome);
        return row;
    }

    private void render(String message) {
        DoubleExpression textWidth = resultScroll.widthProperty().subtract(SCROLL_BAR_ALLOWANCE);
        resultContent.getChildren().clear();
        List<FeedbackLines.Line> lines = FeedbackLines.split(message);
        for (FeedbackLines.Line line : lines) {
            resultContent.getChildren().add(switch (line.kind()) {
                case TITLE -> text(line.text(), lines.size() > 1 ? "result-heading" : "result-title", textWidth);
                case TEXT -> text(line.text(), "result-text", textWidth);
                case FORMAT -> labelled("Format", line.text(), textWidth);
                case EXAMPLE -> labelled("Example", line.text(), textWidth);
            });
        }
        resultScroll.setVvalue(0);
    }

    private static Text text(String value, String styleClass, DoubleExpression width) {
        Text text = new Text(value);
        text.getStyleClass().add(styleClass);
        text.wrappingWidthProperty().bind(width);
        return text;
    }

    /**
     * Returns a captioned command line in a monospace font. Clicking it copies the command,
     * since text in the row cannot be selected.
     */
    private static Node labelled(String caption, String command, DoubleExpression width) {
        Label label = new Label(caption);
        label.getStyleClass().add("result-caption");
        label.setMinWidth(CAPTION_WIDTH);
        Text text = text(command, "result-command", width.subtract(CAPTION_WIDTH));
        text.setStyle(MonospaceFont.style());
        HBox row = new HBox(label, text);
        row.setAlignment(Pos.BASELINE_LEFT);
        row.getStyleClass().add("result-command-row");
        row.setCursor(Cursor.HAND);
        Tooltip.install(row, new Tooltip("Click to copy"));
        row.setOnMouseClicked(event -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(command);
            Clipboard.getSystemClipboard().setContent(content);
        });
        return row;
    }

    private static SVGPath checkMark() {
        SVGPath check = new SVGPath();
        check.setContent("M1 5 L4 8 L9 2");
        check.getStyleClass().add("result-check");
        return check;
    }
}
