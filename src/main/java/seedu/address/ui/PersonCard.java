package seedu.address.ui;

import java.util.Map;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;

/**
 * A compact student card showing only identity, schooling level and subjects.
 * The selected student's full information is displayed by {@link PersonDetailsPanel}.
 */
public class PersonCard extends UiPart<Region> {

    /** Style class for a level in no recognised school stage, or no level at all. */
    public static final String NO_STAGE_STYLE_CLASS = "level-other";

    private static final String FXML = "PersonListCard.fxml";
    /** Width of the initials badge and the gap beside it. */
    private static final double BADGE_WIDTH = 38;
    private static final Map<SchoolingLevel.Stage, String> STAGE_STYLE_CLASSES = Map.of(
            SchoolingLevel.Stage.PRIMARY, "level-primary",
            SchoolingLevel.Stage.SECONDARY, "level-secondary",
            SchoolingLevel.Stage.JC, "level-jc",
            SchoolingLevel.Stage.OTHER, NO_STAGE_STYLE_CLASS);

    public final Student person;

    @FXML
    private Label id;
    @FXML
    private Label initials;
    @FXML
    private VBox summary;
    @FXML
    private Label name;
    @FXML
    private Label schoolingLevel;
    @FXML
    private Label subjects;

    /** Creates a compact card with the index used by commands. */
    public PersonCard(Student person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(String.valueOf(displayedIndex));
        name.setText(person.getName().fullName);
        initials.setText(StudentCardText.initials(person.getName().fullName));
        initials.getStyleClass().add(stageStyleClass(person.getStage()));
        schoolingLevel.setText(person.getSchoolingLevel().map(Object::toString).orElse(""));
        schoolingLevel.setVisible(person.getSchoolingLevel().isPresent());
        schoolingLevel.setManaged(person.getSchoolingLevel().isPresent());
        subjects.setText(StudentCardText.subjects(person).orElse("No lessons yet"));
    }

    /** Keeps long names and subjects within the list column, including when the window is resized. */
    public void bindWidth(DoubleExpression width) {
        size(summary, Bindings.max(0.0, width.subtract(BADGE_WIDTH)));
    }

    /** Returns the style class that colours {@code level} by its school stage. */
    public static String stageStyleClass(String level) {
        return stageStyleClass(SchoolingLevel.stageOf(level));
    }

    /** Returns the style class that colours a school stage, e.g. "level-primary". */
    static String stageStyleClass(SchoolingLevel.Stage stage) {
        return STAGE_STYLE_CLASSES.get(stage);
    }

    /** Fixes {@code cell} to {@code width}, so it lines up with its column. */
    static void size(Region cell, DoubleExpression width) {
        cell.minWidthProperty().bind(width);
        cell.prefWidthProperty().bind(width);
        cell.maxWidthProperty().bind(width);
    }
}
