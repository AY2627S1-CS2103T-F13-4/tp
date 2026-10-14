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
 * A table row that displays a {@code Student}: name and level, subjects, next lesson, rate and guardian.
 * An open row also shows its details: every lesson, the contact details, guardian and tags.
 */
public class PersonCard extends UiPart<Region> {

    /** Style class for a level in no recognised school stage, or no level at all. */
    public static final String NO_STAGE_STYLE_CLASS = "level-other";

    private static final String FXML = "PersonListCard.fxml";
    private static final String EMPTY_VALUE_STYLE_CLASS = "row-empty";
    /** Font sizes used in LightTheme.css, used to measure text before layout. */
    private static final double BODY_FONT_SIZE = 13;
    private static final double NAME_FONT_SIZE = 14;
    /** The gap before the level, which follows the name. */
    private static final double LEVEL_GAP = 10;
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
            StudentCardText.Stage.OTHER, PersonCard.NO_STAGE_STYLE_CLASS);

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
    private HBox nameCell;
    @FXML
    private Label initials;
    @FXML
    private Text name;
    @FXML
    private Text schoolingLevel;
    @FXML
    private HBox subjectsCell;
    @FXML
    private Text subjects;
    @FXML
    private HBox lessonCell;
    @FXML
    private Text nextTime;
    @FXML
    private Text moreLessons;
    @FXML
    private HBox rateCell;
    @FXML
    private Text rate;
    @FXML
    private HBox guardianCell;
    @FXML
    private Text guardianName;
    @FXML
    private FlowPane details;

    /** Text in the details that wraps: lesson locations and contact lines. */
    private final List<Text> lessonLocations = new ArrayList<>();
    private final List<Text> contactLines = new ArrayList<>();
    private final List<StudentCardText.LessonLine> lines;
    private boolean detailsBuilt;
    private boolean open;
    private DoubleExpression detailsWidth;

    /**
     * Creates a row for {@code person} with the index to display. Its details stay hidden until {@link #setOpen}.
     */
    public PersonCard(Student person, int displayedIndex) {
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
        } else {
            hide(schoolingLevel);
        }

        lines = StudentCardText.lessons(person);
        subjects.setText(StudentCardText.subjects(person).orElse("None yet"));
        Optional<StudentCardText.LessonLine> next =
                StudentCardText.nextLesson(lines, StudentCardText.slots(person), LocalDateTime.now());
        if (next.isPresent()) {
            nextTime.setText(StudentCardText.dayAndStart(next.get()));
            moreLessons.setText(lines.size() > 1 ? "+" + (lines.size() - 1) + " more" : "");
            rate.setText(StudentCardText.tableRate(next.get()));
        } else {
            nextTime.setText(StudentCardText.NOT_SET);
            rate.setText("\u2014");
            subjects.getStyleClass().add(EMPTY_VALUE_STYLE_CLASS);
            nextTime.getStyleClass().add(EMPTY_VALUE_STYLE_CLASS);
            rate.getStyleClass().add(EMPTY_VALUE_STYLE_CLASS);
        }
        guardianName.setText(person.getGuardian().map(g -> g.getName().fullName).orElse("None yet"));
        if (person.getGuardian().isEmpty()) {
            guardianName.getStyleClass().add(EMPTY_VALUE_STYLE_CLASS);
        }
        hide(details);
    }

    /**
     * Shows or hides the row's details at once. They are built the first time the row is opened.
     */
    public void setOpen(boolean open) {
        this.open = open;
        if (open && !detailsBuilt) {
            fillDetails(lines);
            bindDetails();
            detailsBuilt = true;
        }
        details.setVisible(open);
        details.setManaged(open);
        markOpen(open);
    }

    /**
     * Opens or closes the row's details with a short animation, for when the user changes which row is open.
     * Does nothing if the row is already in that state.
     */
    public void animateOpen(boolean open) {
        if (open == this.open) {
            return;
        }
        if (open) {
            setOpen(true);
            Motion.expand(details);
        } else {
            this.open = false;
            markOpen(false);
            // Re-opened before the close finished: leave the details showing.
            Motion.collapse(details, () -> {
                if (!this.open) {
                    setOpen(false);
                }
            });
        }
    }

    private void markOpen(boolean open) {
        if (open) {
            if (!cardPane.getStyleClass().contains("student-row-open")) {
                cardPane.getStyleClass().add("student-row-open");
            }
        } else {
            cardPane.getStyleClass().remove("student-row-open");
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
     * In narrow windows the Subjects and Guardian columns are hidden; the details always show both.
     */
    public void bindColumns(PersonListPanel.Columns columns) {
        size(nameCell, columns.name());
        size(subjectsCell, columns.subjects());
        size(lessonCell, columns.lesson());
        size(rateCell, columns.rate());
        size(guardianCell, columns.guardian());
        for (HBox optional : List.of(subjectsCell, guardianCell)) {
            optional.visibleProperty().bind(columns.wide());
            optional.managedProperty().bind(columns.wide());
        }
        double levelWidth = schoolingLevel.isManaged()
                ? textWidth(schoolingLevel.getText(), BODY_FONT_SIZE) + LEVEL_GAP : 0;
        wrapWhenLonger(name, columns.name().subtract(BADGE_WIDTH + levelWidth), NAME_FONT_SIZE);
        wrapWhenLonger(subjects, columns.subjects(), BODY_FONT_SIZE);
        wrapWhenLonger(guardianName, columns.guardian(), BODY_FONT_SIZE);
        detailsWidth = columns.details();
    }

    private void bindDetails() {
        details.prefWrapLengthProperty().bind(detailsWidth);
        DoubleExpression locationLimit = Bindings.min(detailsWidth, LESSON_GRID_WIDTH)
                .subtract(LESSON_COLUMN_WIDTHS[0] + LESSON_COLUMN_GAP + MODE_CHIP_ALLOWANCE);
        lessonLocations.forEach(location -> wrapWhenLonger(location, locationLimit, BODY_FONT_SIZE));
        DoubleExpression contactLimit = Bindings.min(detailsWidth, CONTACT_MAX_WIDTH);
        contactLines.forEach(line -> wrapWhenLonger(line, contactLimit, BODY_FONT_SIZE));
    }

    /** Returns the style class that colours {@code level} by its school stage. */
    public static String stageStyleClass(String level) {
        return STAGE_STYLE_CLASSES.get(StudentCardText.stageOf(level));
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
