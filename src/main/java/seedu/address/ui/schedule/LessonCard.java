package seedu.address.ui.schedule;

import static java.util.Objects.requireNonNull;

import javafx.scene.layout.Region;
import seedu.address.ui.UiPart;

/**
 * A lesson's visual block in the schedule grid.
 */
public class LessonCard extends UiPart<Region> {
    private static final String FXML = "LessonCard.fxml";

    private final ScheduleEntry entry;

    /**
     * Creates a card for the entry, retaining the grid's existing blank lesson appearance.
     */
    LessonCard(ScheduleEntry entry) {
        super(FXML);
        this.entry = requireNonNull(entry);
    }
}
