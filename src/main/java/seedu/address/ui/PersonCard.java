package seedu.address.ui;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import seedu.address.model.person.Student;

/**
 * A UI component that displays information of a {@code Student}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final String EMPTY_VALUE_STYLE_CLASS = "fact-empty";
    private static final String LESSON_STYLE_CLASS = "fact-value";
    private static final double ADDRESS_MAX_WIDTH = 360;
    /** Room left for the level chip beside the tags, which share a row. */
    private static final double LEVEL_CHIP_ALLOWANCE = 120;

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Student person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label id;
    @FXML
    private Text name;
    @FXML
    private HBox badges;
    @FXML
    private Label schoolingLevel;
    @FXML
    private FlowPane tags;
    @FXML
    private VBox lessons;
    @FXML
    private FlowPane facts;
    @FXML
    private Label guardian;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private Text address;

    private final List<Text> lessonLines;

    /**
     * Creates a {@code PersonCard} with the given {@code Student} and index to display.
     */
    public PersonCard(Student person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(String.valueOf(displayedIndex));
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        email.setText(person.getEmail().value);
        showOrPlaceholder(guardian, StudentCardText.guardian(person), StudentCardText.NOT_SET);

        if (person.getSchoolingLevel().isPresent()) {
            schoolingLevel.setText(person.getSchoolingLevel().get().toString());
        } else {
            hide(schoolingLevel);
        }
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        if (person.getTags().isEmpty()) {
            hide(tags);
        }
        if (person.getSchoolingLevel().isEmpty() && person.getTags().isEmpty()) {
            hide(badges);
        }

        List<String> lines = StudentCardText.lessons(person);
        lessonLines = (lines.isEmpty() ? List.of(StudentCardText.NO_LESSONS) : lines).stream()
                .map(Text::new)
                .toList();
        lessonLines.forEach(line -> line.getStyleClass().add(LESSON_STYLE_CLASS));
        if (lines.isEmpty()) {
            lessonLines.get(0).getStyleClass().add(EMPTY_VALUE_STYLE_CLASS);
        }
        lessons.getChildren().addAll(lessonLines);
    }

    /**
     * Wraps the card's text and rows within {@code width}, the space beside the index column.
     * The list asks a card for its height before laying it out, so wrapping has to be set from a known width:
     * FlowPane would otherwise assume 400px, and wrapped labels would report a single line.
     */
    public void bindWrapWidth(DoubleExpression width) {
        tags.prefWrapLengthProperty().bind(width.subtract(LEVEL_CHIP_ALLOWANCE));
        facts.prefWrapLengthProperty().bind(width);
        name.wrappingWidthProperty().bind(width);
        lessonLines.forEach(line -> line.wrappingWidthProperty().bind(width));
        address.wrappingWidthProperty().bind(Bindings.min(width, ADDRESS_MAX_WIDTH));
    }

    /** Shows {@code text} in {@code label}, or a muted {@code placeholder} when the detail is not recorded. */
    private static void showOrPlaceholder(Label label, Optional<String> text, String placeholder) {
        label.setText(text.orElse(placeholder));
        if (text.isEmpty()) {
            label.getStyleClass().add(EMPTY_VALUE_STYLE_CLASS);
        }
    }

    private static void hide(Region region) {
        region.setVisible(false);
        region.setManaged(false);
    }
}
