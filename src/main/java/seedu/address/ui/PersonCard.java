package seedu.address.ui;

import java.time.LocalDateTime;
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
 * A table row that displays a {@code Student}. The row shows only the name, level, next lesson and guardian;
 * an expanded row also shows every lesson, the contact details and tags.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final String EMPTY_VALUE_STYLE_CLASS = "row-empty";
    /** Font sizes used in LightTheme.css, used to measure text before layout. */
    private static final double BODY_FONT_SIZE = 13;
    private static final double NAME_FONT_SIZE = 14;
    private static final double CHIP_FONT_SIZE = 12;
    /** A chip's horizontal padding (8 + 8) plus the gap before it. */
    private static final double CHIP_PADDING_AND_GAP = 26;
    /** Width of the initials badge and the gap after it, beside the name. */
    private static final double BADGE_WIDTH = 38;
    /** Fixed widths of the lesson columns in the details (number, subject, day and time, rate). */
    private static final double[] LESSON_COLUMN_WIDTHS = {20, 110, 148, 76};
    private static final double LESSON_COLUMN_GAP = 14;
    /** Width a lesson line needs in the details: its columns plus the gaps between them. */
    private static final double LESSON_GRID_WIDTH =
            Arrays.stream(LESSON_COLUMN_WIDTHS).sum() + (LESSON_COLUMN_WIDTHS.length - 1) * LESSON_COLUMN_GAP;
    private static final double MODE_CHIP_GAP = 8;
    /** Room for the widest mode chip ("Physical") and its gap, in front of a location line. */
    private static final double MODE_CHIP_ALLOWANCE = 76;
    /** Contact details wrap at this width in the details. */
    private static final double CONTACT_MAX_WIDTH = 320;
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
    private VBox cardPane;
    @FXML
    private Label id;
    @FXML
    private HBox studentCell;
    @FXML
    private Label initials;
    @FXML
    private Text name;
    @FXML
    private Label schoolingLevel;
    @FXML
    private HBox nextLessonCell;
    @FXML
    private Text nextSubject;
    @FXML
    private Text nextTime;
    @FXML
    private Text moreLessons;
    @FXML
    private HBox guardianCell;
    @FXML
    private Text guardianName;
    @FXML
    private FlowPane details;

    /** Text in the details that wraps: lesson locations and contact lines. */
    private final List<Text> lessonLocations = new ArrayList<>();
    private final List<Text> contactLines = new ArrayList<>();

    /**
     * Creates a row for {@code person} with the index to display. An {@code expanded} row also shows the details.
     */
    public PersonCard(Student person, int displayedIndex, boolean expanded) {
        super(FXML);
        this.person = person;
        id.setText(String.valueOf(displayedIndex));
        name.setText(person.getName().fullName);
        initials.setText(StudentCardText.initials(person.getName().fullName));

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

        List<StudentCardText.LessonLine> lines = StudentCardText.lessons(person);
        Optional<StudentCardText.LessonLine> next =
                StudentCardText.nextLesson(lines, StudentCardText.slots(person), LocalDateTime.now());
        if (next.isPresent()) {
            nextSubject.setText(next.get().subject());
            nextTime.setText(StudentCardText.dayAndStart(next.get()));
            moreLessons.setText(lines.size() > 1 ? "+" + (lines.size() - 1) + " more" : "");
        } else {
            nextSubject.setText(StudentCardText.NO_LESSONS);
            nextSubject.getStyleClass().setAll("row-muted");
        }
        guardianName.setText(person.getGuardian().map(g -> g.getName().fullName).orElse(StudentCardText.NOT_SET));
        if (person.getGuardian().isEmpty()) {
            guardianName.getStyleClass().add(EMPTY_VALUE_STYLE_CLASS);
        }

        if (expanded) {
            cardPane.getStyleClass().add("student-row-expanded");
            fillDetails(lines);
        } else {
            hide(details);
        }
    }

    /** Fills the details: every lesson, the contact details, the guardian and any tags. */
    private void fillDetails(List<StudentCardText.LessonLine> lines) {
        Node lessonContent = lines.isEmpty() ? muted(StudentCardText.NO_LESSONS) : lessonTable(lines);
        details.getChildren().add(section("Lessons", lessonContent));

        VBox contact = new VBox(3);
        for (String line : List.of(person.getPhone().value, person.getEmail().value, person.getAddress().value)) {
            Text text = text(line, "row-value");
            contactLines.add(text);
            contact.getChildren().add(text);
        }
        details.getChildren().add(section("Contact", contact));

        VBox guardian = new VBox(3);
        if (person.getGuardian().isPresent()) {
            guardian.getChildren().addAll(text(person.getGuardian().get().getName().fullName, "row-value"),
                    text(person.getGuardian().get().getPhone().value, "row-secondary"));
        } else {
            guardian.getChildren().add(muted(StudentCardText.NOT_SET));
        }
        details.getChildren().add(section("Guardian", guardian));

        if (!person.getTags().isEmpty()) {
            FlowPane tags = new FlowPane(6, 6);
            person.getTags().stream()
                    .sorted(Comparator.comparing(tag -> tag.tagName))
                    .forEach(tag -> {
                        Label label = new Label(tag.tagName);
                        label.getStyleClass().addAll("chip", "tag-chip");
                        tags.getChildren().add(label);
                    });
            tags.setPrefWrapLength(CONTACT_MAX_WIDTH);
            details.getChildren().add(section("Tags", tags));
        }
    }

    /**
     * Lays out every lesson: number, subject, day and time, and rate line up in columns, and a lesson's mode and
     * location sit on a second line under its subject. Numbers match {@code LESSON_INDEX}.
     */
    private GridPane lessonTable(List<StudentCardText.LessonLine> lines) {
        GridPane table = new GridPane();
        for (double width : LESSON_COLUMN_WIDTHS) {
            table.getColumnConstraints().add(new ColumnConstraints(width));
        }
        table.setHgap(LESSON_COLUMN_GAP);
        table.setVgap(4);
        int row = 0;
        for (StudentCardText.LessonLine line : lines) {
            table.addRow(row++, text(line.number() + ".", "lesson-number"), text(line.subject(), "lesson-subject"),
                    text(line.slot(), "row-value"), text(line.rate(), "row-value"));
            if (line.mode().isEmpty() && line.location().isEmpty()) {
                continue;
            }
            HBox extra = new HBox(MODE_CHIP_GAP);
            extra.setAlignment(Pos.CENTER_LEFT);
            line.mode().ifPresent(lessonMode -> {
                Label chip = new Label(lessonMode.toString());
                chip.getStyleClass().addAll("chip", "mode-chip", "mode-" + lessonMode.name().toLowerCase(Locale.ROOT));
                chip.setMinWidth(Region.USE_PREF_SIZE);
                extra.getChildren().add(chip);
            });
            line.location().ifPresent(location -> {
                Text text = text("at " + location, "row-secondary");
                lessonLocations.add(text);
                extra.getChildren().add(text);
            });
            table.add(extra, 1, row++, 3, 1);
        }
        return table;
    }

    /**
     * Sizes the row's cells to the table's columns, and wraps text within them.
     * The list asks a row for its height before laying it out, so wrapping has to be set from known widths.
     * In narrow windows the Guardian column is hidden; the details always show the guardian with their phone.
     */
    public void bindColumns(DoubleExpression studentWidth, DoubleExpression lessonWidth,
            DoubleExpression guardianWidth, BooleanExpression wide, DoubleExpression detailsWidth) {
        size(studentCell, studentWidth);
        size(nextLessonCell, lessonWidth);
        size(guardianCell, guardianWidth);
        guardianCell.visibleProperty().bind(wide);
        guardianCell.managedProperty().bind(wide);

        double levelWidth = schoolingLevel.isManaged()
                ? textWidth(schoolingLevel.getText(), CHIP_FONT_SIZE) + CHIP_PADDING_AND_GAP : 0;
        wrapWhenLonger(name, studentWidth.subtract(BADGE_WIDTH + levelWidth), NAME_FONT_SIZE);
        wrapWhenLonger(guardianName, guardianWidth, BODY_FONT_SIZE);

        details.prefWrapLengthProperty().bind(detailsWidth);
        DoubleExpression locationLimit = Bindings.min(detailsWidth, LESSON_GRID_WIDTH)
                .subtract(LESSON_COLUMN_WIDTHS[0] + LESSON_COLUMN_GAP + MODE_CHIP_ALLOWANCE);
        lessonLocations.forEach(location -> wrapWhenLonger(location, locationLimit, BODY_FONT_SIZE));
        DoubleExpression contactLimit = Bindings.min(detailsWidth, CONTACT_MAX_WIDTH);
        contactLines.forEach(line -> wrapWhenLonger(line, contactLimit, BODY_FONT_SIZE));
    }

    private static VBox section(String caption, Node content) {
        Label label = new Label(caption);
        label.getStyleClass().add("detail-caption");
        return new VBox(8, label, content);
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

    private static Text muted(String value) {
        Text text = text(value, "row-muted");
        text.getStyleClass().add(EMPTY_VALUE_STYLE_CLASS);
        return text;
    }

    private static void hide(Node node) {
        node.setVisible(false);
        node.setManaged(false);
    }
}
