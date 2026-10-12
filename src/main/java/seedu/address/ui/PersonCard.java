package seedu.address.ui;

import java.util.Comparator;
import java.util.Optional;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import seedu.address.model.person.Person;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final String EMPTY_VALUE_STYLE_CLASS = "fact-empty";
    private static final double LEVEL_CHIP_ALLOWANCE = 120;
    private static final double ADDRESS_MAX_WIDTH = 360;

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Text name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Text address;
    @FXML
    private Label email;
    @FXML
    private Text subject;
    @FXML
    private Label schoolingLevel;
    @FXML
    private Label weeklyLessonSlot;
    @FXML
    private Label hourlyRate;
    @FXML
    private Label guardian;
    @FXML
    private FlowPane tags;
    @FXML
    private FlowPane facts;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(String.valueOf(displayedIndex));
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        email.setText(person.getEmail().value);
        subject.setText(StudentCardText.subjects(person).orElse(StudentCardText.NO_SUBJECTS));
        if (person.getSubjects().isEmpty()) {
            subject.getStyleClass().add(EMPTY_VALUE_STYLE_CLASS);
        }
        showOrPlaceholder(weeklyLessonSlot, StudentCardText.lesson(person), StudentCardText.NOT_SET);
        showOrPlaceholder(hourlyRate, StudentCardText.rate(person), StudentCardText.NOT_SET);
        showOrPlaceholder(guardian, StudentCardText.guardian(person), StudentCardText.NOT_SET);
        if (person.getSchoolingLevel().isPresent()) {
            schoolingLevel.setText(person.getSchoolingLevel().get().toString());
        } else {
            schoolingLevel.setVisible(false);
            schoolingLevel.setManaged(false);
        }
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        if (tags.getChildren().isEmpty()) {
            tags.setVisible(false);
            tags.setManaged(false);
        }
    }

    /**
     * Wraps the card's text and rows within {@code width}, the space beside the index column.
     * The list asks a card for its height before laying it out, so wrapping has to be set from a known width:
     * FlowPane would otherwise assume 400px, and wrapped labels would report a single line.
     */
    public void bindWrapWidth(DoubleExpression width) {
        tags.prefWrapLengthProperty().bind(width);
        facts.prefWrapLengthProperty().bind(width);
        name.wrappingWidthProperty().bind(width);
        subject.wrappingWidthProperty().bind(width.subtract(LEVEL_CHIP_ALLOWANCE));
        address.wrappingWidthProperty().bind(Bindings.min(width, ADDRESS_MAX_WIDTH));
    }

    /** Shows {@code text} in {@code label}, or a muted {@code placeholder} when the detail is not recorded. */
    private static void showOrPlaceholder(Label label, Optional<String> text, String placeholder) {
        label.setText(text.orElse(placeholder));
        if (text.isEmpty()) {
            label.getStyleClass().add(EMPTY_VALUE_STYLE_CLASS);
        }
    }
}
