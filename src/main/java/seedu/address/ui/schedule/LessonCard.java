package seedu.address.ui.schedule;

import static java.util.Objects.requireNonNull;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import seedu.address.model.person.Lesson;
import seedu.address.ui.UiPart;

/**
 * A lesson's visual block in the schedule grid.
 */
public class LessonCard extends UiPart<Region> {
    private static final String FXML = "LessonCard.fxml";

    private final ScheduleEntry entry;

    /**
     * Creates a card showing the student, subject and time, with full lesson details on hover.
     */
    LessonCard(ScheduleEntry entry) {
        super(FXML);
        this.entry = requireNonNull(entry);
        populateCard();
    }

    private void populateCard() {
        Lesson lesson = entry.getLesson();
        String studentName = entry.getStudent().getName().fullName;
        VBox card = (VBox) getRoot();
        card.getStyleClass().add("lesson-card");
        // Clear the FXML's inline background so the stylesheet controls the card's appearance.
        card.setStyle("");
        card.setMinSize(0, 0);
        card.setPrefSize(0, 0);

        Label name = createLabel(studentName);
        name.getStyleClass().add("lesson-card-name");
        Label subject = createLabel(lesson.getSubject().toString());
        Label time = createLabel(entry.getStart() + "-" + entry.getEnd());
        card.getChildren().addAll(name, subject, time);

        // Keep short lessons within their grid cells without expanding the schedule's rows.
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        card.setClip(clip);

        String details = studentName + "\n" + lesson;
        Tooltip tooltip = new Tooltip(details);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(320);
        Tooltip.install(card, tooltip);
        card.setAccessibleText(details);
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setMinWidth(0);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }
}
