package seedu.address.ui.schedule;

import static java.util.Objects.requireNonNull;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.WeeklyLessonSlot;
import seedu.address.ui.PersonCard;
import seedu.address.ui.UiPart;

/**
 * A lesson's visual block in the schedule grid.
 */
public class LessonCard extends UiPart<Region> {
    private static final String FXML = "LessonCard.fxml";
    private static final double TOOLTIP_MAX_WIDTH = 320;
    private static final String DETAIL_SEPARATOR = " \u00b7 ";
    private static final double COMPACT_SPACING = 4;

    private final ScheduleEntry entry;
    private final int lineCount;

    /**
     * Creates a card showing the lesson's number (e.g. "#3.2"), student, subject and time,
     * with full lesson details on hover. A block too short for that has a {@code lineCount} under 3:
     * one line shows the number and the student, and two lines add the time.
     */
    LessonCard(ScheduleEntry entry, int lineCount) {
        super(FXML);
        assert lineCount >= 1 && lineCount <= 3;
        this.entry = requireNonNull(entry);
        this.lineCount = lineCount;
        populateCard();
    }

    private void populateCard() {
        Lesson lesson = entry.getLesson();
        String studentName = entry.getStudent().getName().fullName;
        VBox card = (VBox) getRoot();
        card.getStyleClass().addAll("lesson-card", stageStyleClass());
        card.setMinSize(0, 0);
        card.setPrefSize(0, 0);

        Label time = createLabel(WeeklyLessonSlot.formatTime(entry.getStart()) + "-"
                + WeeklyLessonSlot.formatTime(entry.getEnd()));
        if (lineCount < 3) {
            // The number comes first and never shrinks, so a narrow block cuts the name, not the number.
            Label number = new Label(entry.getCommandLabel());
            number.setMinWidth(Region.USE_PREF_SIZE);
            Label name = createLabel(studentName);
            number.getStyleClass().add("lesson-card-name");
            name.getStyleClass().add("lesson-card-name");
            card.getStyleClass().add("lesson-card-compact");
            card.getChildren().add(new HBox(COMPACT_SPACING, number, name));
            if (lineCount == 2) {
                card.getChildren().add(time);
            }
        } else {
            // Name, then time, then the details, which include the number.
            Label name = createLabel(studentName);
            name.getStyleClass().add("lesson-card-name");
            Label details = createLabel(detailLine(lesson));
            card.getChildren().addAll(name, time, details);
        }

        // Keep short lessons within their grid cells without expanding the schedule's rows.
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        card.setClip(clip);

        String fullDetails = entry.getCommandLabel() + " " + studentName + "\n" + lesson;
        Tooltip tooltip = new Tooltip(fullDetails);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(TOOLTIP_MAX_WIDTH);
        // JavaFX hides tooltips after 5s by default; keep it up until the cursor leaves the card.
        tooltip.setShowDuration(Duration.INDEFINITE);
        Tooltip.install(card, tooltip);
        card.setAccessibleText(fullDetails);
    }

    /**
     * Returns the lesson's number, the student's level if any, and the subject, e.g. "#3.1 · Sec 3 · A-Math".
     * The number is what commands use, as in {@code lesson-edit 3 1}.
     */
    private String detailLine(Lesson lesson) {
        String level = entry.getStudent().getSchoolingLevel().map(value -> value + DETAIL_SEPARATOR).orElse("");
        return entry.getCommandLabel() + DETAIL_SEPARATOR + level + lesson.getSubject();
    }

    /** Returns the style class that colours the card by the student's school stage, as in the student table. */
    private String stageStyleClass() {
        return entry.getStudent().getSchoolingLevel()
                .map(level -> PersonCard.stageStyleClass(level.toString()))
                .orElse(PersonCard.NO_STAGE_STYLE_CLASS);
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setMinWidth(0);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }
}
