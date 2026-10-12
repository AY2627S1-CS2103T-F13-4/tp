package seedu.address.ui;

import java.util.Comparator;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Student;

/**
 * A UI component that displays information of a {@code Student}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

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
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private Label schoolingLevel;
    @FXML
    private Label lessons;
    @FXML
    private Label guardian;
    @FXML
    private FlowPane tags;

    /**
     * Creates a {@code PersonCard} with the given {@code Student} and index to display.
     */
    public PersonCard(Student person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        email.setText(person.getEmail().value);
        guardian.setText("Guardian: " + person.getGuardian().map(Object::toString).orElse("Not recorded"));
        if (person.getSchoolingLevel().isPresent()) {
            schoolingLevel.setText("Level: " + person.getSchoolingLevel().get());
        } else {
            schoolingLevel.setVisible(false);
            schoolingLevel.setManaged(false);
        }
        if (person.getLessons().isEmpty()) {
            lessons.setText("Lessons: Not recorded");
        } else {
            lessons.setText("Lessons:\n" + person.getLessons().stream()
                    .map(lesson -> "- " + lesson)
                    .collect(Collectors.joining("\n")));
        }
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
    }
}
