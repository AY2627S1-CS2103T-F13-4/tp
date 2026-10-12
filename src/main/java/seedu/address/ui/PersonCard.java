package seedu.address.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.DoubleExpression;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import seedu.address.model.person.Student;

/**
 * A table row that displays a {@code Student}: number, student details, a breakdown of their lessons, and guardian.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final String EMPTY_VALUE_STYLE_CLASS = "row-empty";
    /** Font sizes used in LightTheme.css, used to measure text before layout. */
    private static final double BODY_FONT_SIZE = 13;
    private static final double MUTED_FONT_SIZE = 12.5;
    /** Gaps in a student's lesson breakdown, and between a lesson's mode chip and its location. */
    private static final double LESSON_COLUMN_GAP = 14;
    private static final double LESSON_ROW_GAP = 4;
    private static final double MODE_CHIP_GAP = 8;
    /** Fixed widths of the lesson columns (number, subject, day and time, rate), so they line up across rows. */
    private static final double[] LESSON_COLUMN_WIDTHS = {20, 100, 148, 76};
    /** Width a lesson line needs: its columns plus the gaps between them. */
    static final double LESSON_GRID_WIDTH =
            Arrays.stream(LESSON_COLUMN_WIDTHS).sum() + (LESSON_COLUMN_WIDTHS.length - 1) * LESSON_COLUMN_GAP;
    private static final double NAME_FONT_SIZE = 14;
    private static final double CHIP_FONT_SIZE = 12;
    /** A chip's horizontal padding (8 + 8) plus the gap before it. */
    private static final double CHIP_PADDING_AND_GAP = 26;
    /** Room for the widest mode chip ("Physical") and its gap, in front of a location line. */
    private static final double MODE_CHIP_ALLOWANCE = 76;
    /** Width of the initials badge and the gap after it, beside the name. */
    private static final double BADGE_WIDTH = 42;
    /** Colours the level chip and initials badge by school stage; unrecognised levels stay neutral. */
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
    private VBox studentCell;
    @FXML
    private Label initials;
    @FXML
    private Text name;
    @FXML
    private FlowPane badges;
    @FXML
    private Label schoolingLevel;
    @FXML
    private Text contact;
    @FXML
    private Text address;
    @FXML
    private Text guardianInline;
    @FXML
    private VBox lessons;
    @FXML
    private VBox guardianCell;
    @FXML
    private Text guardianName;
    @FXML
    private Text guardianPhone;

    /** Text in the lesson breakdown that wraps: long subjects, and each lesson's location line. */
    private final List<Text> lessonSubjects = new ArrayList<>();
    private final List<Text> lessonLocations = new ArrayList<>();
    private Text noLessons;

    /**
     * Creates a {@code PersonCard} with the given {@code Student} and index to display.
     */
    public PersonCard(Student person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(String.valueOf(displayedIndex));
        name.setText(person.getName().fullName);
        initials.setText(StudentCardText.initials(person.getName().fullName));
        contact.setText(person.getPhone().value + "  ·  " + person.getEmail().value);
        address.setText(person.getAddress().value);

        String stageClass = person.getSchoolingLevel()
                .map(level -> STAGE_STYLE_CLASSES.get(StudentCardText.stageOf(level.toString())))
                .orElse("level-other");
        initials.getStyleClass().add(stageClass);
        if (person.getSchoolingLevel().isPresent()) {
            schoolingLevel.setText(person.getSchoolingLevel().get().toString());
            schoolingLevel.getStyleClass().add(stageClass);
        } else {
            hide(schoolingLevel);
        }
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> {
                    Label label = new Label(tag.tagName);
                    label.getStyleClass().addAll("chip", "tag-chip");
                    badges.getChildren().add(label);
                });
        if (person.getTags().isEmpty()) {
            hide(badges);
        }

        Optional<String> guardian = StudentCardText.guardian(person);
        guardianName.setText(person.getGuardian().map(g -> g.getName().fullName).orElse(StudentCardText.NOT_SET));
        guardianPhone.setText(person.getGuardian().map(g -> g.getPhone().value).orElse(""));
        guardianInline.setText("Guardian: " + guardian.orElse(StudentCardText.NOT_SET));
        if (guardian.isEmpty()) {
            guardianName.getStyleClass().add(EMPTY_VALUE_STYLE_CLASS);
            guardianPhone.setManaged(false);
        }

        List<StudentCardText.LessonLine> lines = StudentCardText.lessons(person);
        if (lines.isEmpty()) {
            noLessons = new Text(StudentCardText.NO_LESSONS);
            noLessons.getStyleClass().addAll("row-value", EMPTY_VALUE_STYLE_CLASS);
            lessons.getChildren().add(noLessons);
        } else {
            lessons.getChildren().add(lessonTable(lines));
        }
    }

    /**
     * Lays out a student's lessons: number, subject, day and time, and rate line up in columns, and a lesson's
     * mode and location sit on a second line under its subject. Numbers match {@code LESSON_INDEX}.
     */
    private GridPane lessonTable(List<StudentCardText.LessonLine> lines) {
        GridPane table = new GridPane();
        for (double width : LESSON_COLUMN_WIDTHS) {
            table.getColumnConstraints().add(new ColumnConstraints(width));
        }
        table.setHgap(LESSON_COLUMN_GAP);
        table.setVgap(LESSON_ROW_GAP);
        int row = 0;
        for (StudentCardText.LessonLine line : lines) {
            Text number = text(line.number() + ".", "lesson-number");
            Text subject = text(line.subject(), "lesson-subject");
            lessonSubjects.add(subject);
            table.addRow(row++, number, subject, text(line.slot(), "row-value"), text(line.rate(), "row-value"));

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
                Text text = text("at " + location, "row-muted");
                lessonLocations.add(text);
                details.getChildren().add(text);
            });
            table.add(details, 1, row++, 3, 1);
        }
        return table;
    }

    /**
     * Sizes the row's cells to the table's columns, and wraps their text within them.
     * The list asks a row for its height before laying it out, so wrapping has to be set from known widths.
     * In narrow windows the Guardian column is hidden and the guardian is shown in the Student cell instead.
     */
    public void bindColumns(DoubleExpression studentWidth, DoubleExpression lessonsWidth,
            DoubleExpression guardianWidth, BooleanExpression wide) {
        size(studentCell, studentWidth);
        size(lessons, lessonsWidth);
        size(guardianCell, guardianWidth);
        guardianCell.visibleProperty().bind(wide);
        guardianCell.managedProperty().bind(wide);
        guardianInline.visibleProperty().bind(wide.not());
        guardianInline.managedProperty().bind(wide.not());

        badges.prefWrapLengthProperty().bind(studentWidth);
        // The name shares its line with the initials badge and the level chip.
        double levelWidth = schoolingLevel.isManaged()
                ? textWidth(schoolingLevel.getText(), CHIP_FONT_SIZE) + CHIP_PADDING_AND_GAP : 0;
        wrapWhenLonger(name, studentWidth.subtract(BADGE_WIDTH + levelWidth), NAME_FONT_SIZE);
        wrapWhenLonger(contact, studentWidth, MUTED_FONT_SIZE);
        wrapWhenLonger(address, studentWidth, MUTED_FONT_SIZE);
        wrapWhenLonger(guardianInline, studentWidth, MUTED_FONT_SIZE);
        wrapWhenLonger(guardianName, guardianWidth, BODY_FONT_SIZE);

        lessonSubjects.forEach(subject -> subject.setWrappingWidth(
                textWidth(subject.getText(), BODY_FONT_SIZE) > LESSON_COLUMN_WIDTHS[1] ? LESSON_COLUMN_WIDTHS[1] : 0));
        DoubleExpression locationLimit = lessonsWidth.subtract(LESSON_COLUMN_WIDTHS[0] + LESSON_COLUMN_GAP
                + MODE_CHIP_ALLOWANCE);
        lessonLocations.forEach(location -> wrapWhenLonger(location, locationLimit, MUTED_FONT_SIZE));
        if (noLessons != null) {
            wrapWhenLonger(noLessons, lessonsWidth, BODY_FONT_SIZE);
        }
    }

    private static void size(Region cell, DoubleExpression width) {
        cell.minWidthProperty().bind(width);
        cell.prefWidthProperty().bind(width);
        cell.maxWidthProperty().bind(width);
    }

    /**
     * Wraps {@code text} at {@code limit} only when it is wider than that.
     * A {@code Text} with a wrapping width always takes that whole width, which would spread short values apart.
     */
    private static void wrapWhenLonger(Text text, DoubleExpression limit, double fontSize) {
        double natural = textWidth(text.getText(), fontSize);
        text.wrappingWidthProperty().bind(Bindings.createDoubleBinding(() ->
                natural > limit.get() ? limit.get() : 0, limit));
    }

    /** Returns the width of {@code value} in the bundled body font at {@code size}, as styled in LightTheme.css. */
    private static double textWidth(String value, double size) {
        Text measure = new Text(value);
        measure.setFont(Font.font("Geist", size));
        return measure.getLayoutBounds().getWidth();
    }

    private static Text text(String value, String styleClass) {
        Text text = new Text(value);
        text.getStyleClass().add(styleClass);
        return text;
    }

    private static void hide(Node node) {
        node.setVisible(false);
        node.setManaged(false);
    }
}
