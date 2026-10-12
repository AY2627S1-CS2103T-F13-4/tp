package seedu.address.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
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
    /** Font sizes of the level chip and of fact values in LightTheme.css, used to measure text before layout. */
    private static final double CHIP_FONT_SIZE = 12;
    private static final double FACT_FONT_SIZE = 13;
    /** The chip's horizontal padding (8 + 8) plus the 6px gap before the tags. */
    private static final double CHIP_PADDING_AND_GAP = 22;
    /** A chip's horizontal padding (8 + 8), and the gap between a lesson and its mode chip. */
    private static final double CHIP_PADDING = 16;
    private static final double MODE_CHIP_GAP = 8;
    /** Colours the level chip by school stage; unrecognised levels stay neutral. */
    private static final Map<StudentCardText.Stage, String> STAGE_STYLE_CLASSES = Map.of(
            StudentCardText.Stage.PRIMARY, "level-primary",
            StudentCardText.Stage.SECONDARY, "level-secondary",
            StudentCardText.Stage.JC, "level-jc",
            StudentCardText.Stage.OTHER, "level-other");

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
    private Text guardian;
    @FXML
    private Label phone;
    @FXML
    private Text email;
    @FXML
    private Text address;

    /** Each lesson's text, with the mode chip beside it if the lesson has a mode. */
    private final List<Text> lessonTexts = new ArrayList<>();
    private final List<Optional<Label>> lessonModes = new ArrayList<>();

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
        Optional<String> guardianText = StudentCardText.guardian(person);
        guardian.setText(guardianText.orElse(StudentCardText.NOT_SET));
        if (guardianText.isEmpty()) {
            guardian.getStyleClass().add(EMPTY_VALUE_STYLE_CLASS);
        }

        if (person.getSchoolingLevel().isPresent()) {
            String level = person.getSchoolingLevel().get().toString();
            schoolingLevel.setText(level);
            schoolingLevel.getStyleClass().add(STAGE_STYLE_CLASSES.get(StudentCardText.stageOf(level)));
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

        List<StudentCardText.LessonLine> lines = StudentCardText.lessons(person);
        if (lines.isEmpty()) {
            Text none = new Text(StudentCardText.NO_LESSONS);
            none.getStyleClass().addAll(LESSON_STYLE_CLASS, EMPTY_VALUE_STYLE_CLASS);
            lessonTexts.add(none);
            lessonModes.add(Optional.empty());
            lessons.getChildren().add(none);
        }
        for (StudentCardText.LessonLine line : lines) {
            Text text = new Text(line.text());
            text.getStyleClass().add(LESSON_STYLE_CLASS);
            Optional<Label> mode = line.mode().map(lessonMode -> {
                Label chip = new Label(lessonMode.toString());
                chip.getStyleClass().addAll("chip", "mode-chip", "mode-" + lessonMode.name().toLowerCase(Locale.ROOT));
                chip.setMinWidth(Region.USE_PREF_SIZE);
                return chip;
            });
            HBox row = new HBox(MODE_CHIP_GAP, text);
            row.setAlignment(Pos.CENTER_LEFT);
            mode.ifPresent(row.getChildren()::add);
            lessonTexts.add(text);
            lessonModes.add(mode);
            lessons.getChildren().add(row);
        }
    }

    /**
     * Wraps the card's text and rows within {@code width}, the space beside the index column.
     * The list asks a card for its height before laying it out, so wrapping has to be set from a known width:
     * FlowPane would otherwise assume 400px, and wrapped labels would report a single line.
     */
    public void bindWrapWidth(DoubleExpression width) {
        // The tags share a row with the level chip, so they get the width the chip leaves.
        double chipWidth = schoolingLevel.isManaged()
                ? textWidth(schoolingLevel.getText(), CHIP_FONT_SIZE) + CHIP_PADDING_AND_GAP : 0;
        tags.prefWrapLengthProperty().bind(width.subtract(chipWidth));
        facts.prefWrapLengthProperty().bind(width);
        name.wrappingWidthProperty().bind(width);
        for (int i = 0; i < lessonTexts.size(); i++) {
            // A lesson's mode chip follows its text, so the text wraps in the width the chip leaves.
            double modeWidth = lessonModes.get(i)
                    .map(chip -> textWidth(chip.getText(), CHIP_FONT_SIZE) + CHIP_PADDING + MODE_CHIP_GAP).orElse(0.0);
            wrapWhenLonger(lessonTexts.get(i), width.subtract(modeWidth));
        }
        wrapWhenLonger(guardian, width);
        wrapWhenLonger(email, width);
        wrapWhenLonger(address, Bindings.min(width, ADDRESS_MAX_WIDTH));
    }

    /**
     * Wraps {@code text} at {@code limit} only when it is wider than that.
     * A {@code Text} with a wrapping width always takes that whole width, which would spread short values apart.
     */
    private static void wrapWhenLonger(Text text, DoubleExpression limit) {
        double natural = textWidth(text.getText(), FACT_FONT_SIZE);
        text.wrappingWidthProperty().bind(Bindings.createDoubleBinding(() ->
                natural > limit.get() ? limit.get() : 0, limit));
    }

    /** Returns the width of {@code value} in the system font at {@code size}, as styled in LightTheme.css. */
    private static double textWidth(String value, double size) {
        Text measure = new Text(value);
        measure.setFont(Font.font(size));
        return measure.getLayoutBounds().getWidth();
    }

    private static void hide(Region region) {
        region.setVisible(false);
        region.setManaged(false);
    }
}
