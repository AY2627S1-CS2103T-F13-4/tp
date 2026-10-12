package seedu.address.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
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
    /** Gaps in the lessons table, and between a lesson's mode chip and its location. */
    private static final double LESSON_COLUMN_GAP = 16;
    private static final double LESSON_ROW_GAP = 3;
    private static final double MODE_CHIP_GAP = 8;
    /** Room for the widest mode chip ("Physical") and its gap, in front of a location line. */
    private static final double MODE_CHIP_ALLOWANCE = 76;
    /** Subjects longer than this wrap, so the slot and rate columns still fit a 600px window. */
    private static final double SUBJECT_MAX_WIDTH = 150;
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

    /** Text that wraps within the lessons table: long subjects, and each lesson's location line. */
    private final List<Text> lessonSubjects = new ArrayList<>();
    private final List<Text> lessonLocations = new ArrayList<>();
    /** Width of the lesson number column, which the location line is indented by. */
    private double lessonNumberWidth;

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
            lessons.getChildren().add(none);
        } else {
            lessons.getChildren().add(lessonTable(lines));
        }
    }

    /**
     * Lays the lessons out as a small table: number, subject, day and time, and rate line up in columns, and a
     * lesson's mode and location sit on a second line under its subject. Numbers match {@code LESSON_INDEX}.
     */
    private GridPane lessonTable(List<StudentCardText.LessonLine> lines) {
        GridPane table = new GridPane();
        table.setHgap(LESSON_COLUMN_GAP);
        table.setVgap(LESSON_ROW_GAP);
        int row = 0;
        for (StudentCardText.LessonLine line : lines) {
            Text number = new Text(line.number() + ".");
            number.getStyleClass().add("lesson-number");
            lessonNumberWidth = Math.max(lessonNumberWidth, textWidth(number.getText(), FACT_FONT_SIZE));
            Text subject = new Text(line.subject());
            subject.getStyleClass().add("lesson-subject");
            lessonSubjects.add(subject);
            table.addRow(row++, number, subject, lessonText(line.slot()), lessonText(line.rate()));

            if (line.mode().isEmpty() && line.location().isEmpty()) {
                continue;
            }
            HBox details = new HBox(MODE_CHIP_GAP);
            details.setAlignment(Pos.CENTER_LEFT);
            line.mode().ifPresent(lessonMode -> {
                Label chip = new Label(lessonMode.toString());
                chip.getStyleClass().addAll("chip", "mode-chip", "mode-" + lessonMode.name().toLowerCase(Locale.ROOT));
                chip.setMinWidth(Region.USE_PREF_SIZE);
                details.getChildren().add(chip);
            });
            line.location().ifPresent(location -> {
                Text text = new Text("at " + location);
                text.getStyleClass().add("lesson-location");
                lessonLocations.add(text);
                details.getChildren().add(text);
            });
            table.add(details, 1, row++, 3, 1);
        }
        return table;
    }

    private static Text lessonText(String value) {
        Text text = new Text(value);
        text.getStyleClass().add(LESSON_STYLE_CLASS);
        return text;
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
        // Long subjects wrap within their column; a location line can use the width beside the number column,
        // less the mode chip in front of it.
        lessonSubjects.forEach(subject -> wrapWhenLonger(subject, new SimpleDoubleProperty(SUBJECT_MAX_WIDTH)));
        DoubleExpression locationWidth = width.subtract(lessonNumberWidth + LESSON_COLUMN_GAP + MODE_CHIP_ALLOWANCE);
        lessonLocations.forEach(location -> wrapWhenLonger(location, locationWidth));
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
