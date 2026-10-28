package seedu.address.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import javafx.geometry.VPos;
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
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.ProgressNote;
import seedu.address.model.person.SchoolingLevel;
import seedu.address.model.person.Student;

/**
 * A table row that displays a {@code Student}: name and level, subjects, next lesson, rate and guardian.
 * An open row groups school and contact details, followed by the guardian, lessons, tags and progress notes.
 */
public class PersonCard extends UiPart<Region> {

    /** Style class for a level in no recognised school stage, or no level at all. */
    public static final String NO_STAGE_STYLE_CLASS = "level-other";

    private static final String FXML = "PersonListCard.fxml";
    private static final String EMPTY_VALUE_STYLE_CLASS = "row-empty";
    /** Fonts used in LightTheme.css, used to measure text before layout. */
    private static final String BODY_FONT_FAMILY = "Geist";
    private static final String NAME_FONT_FAMILY = "Geist Medium";
    private static final double BODY_FONT_SIZE = 13;
    private static final double NAME_FONT_SIZE = 14;
    private static final double LEVEL_FONT_SIZE = 12.5;
    /** The gap before the level, which follows the name. */
    private static final double LEVEL_GAP = 10;
    /** Width of the initials badge and the gap after it, beside the name. */
    private static final double BADGE_WIDTH = 38;
    /** Share of the name column, after the badge, the name keeps before a long level wraps beside it. */
    private static final double NAME_SHARE = 0.6;
    /** Fixed widths of the lesson columns in the details (number, subject, day and time, rate). */
    private static final double[] LESSON_COLUMN_WIDTHS = {20, 110, 148, 76};
    private static final double LESSON_COLUMN_GAP = 14;
    /** Width a lesson line needs in the details: its columns plus the gaps between them. */
    private static final double LESSON_GRID_WIDTH =
            Arrays.stream(LESSON_COLUMN_WIDTHS).sum() + (LESSON_COLUMN_WIDTHS.length - 1) * LESSON_COLUMN_GAP;
    private static final double MODE_CHIP_GAP = 8;
    /** Room for the widest mode chip ("Physical") and its gap, in front of a location line. */
    private static final double MODE_CHIP_ALLOWANCE = 76;
    /** Contact details and school wrap at this width in the details. */
    private static final double CONTACT_MAX_WIDTH = 320;
    /** Fixed widths of the note number and date columns; the note's text takes the rest, up to its own limit. */
    private static final double[] NOTE_COLUMN_WIDTHS = {20, 84};
    private static final double NOTE_COLUMN_GAP = 14;
    /** A note's number, date and text wrap within this width, so long notes stay readable. */
    private static final double NOTES_MAX_WIDTH = 560;
    private static final DateTimeFormatter NOTE_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);
    /** Colours the level chip and initials badge by school stage; unrecognised levels stay neutral. */
    private static final Map<SchoolingLevel.Stage, String> STAGE_STYLE_CLASSES = Map.of(
            SchoolingLevel.Stage.PRIMARY, "level-primary",
            SchoolingLevel.Stage.SECONDARY, "level-secondary",
            SchoolingLevel.Stage.JC, "level-jc",
            SchoolingLevel.Stage.OTHER, PersonCard.NO_STAGE_STYLE_CLASS);

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

    /** Text in the details that wraps: lesson locations, contact lines, school and note text. */
    private final List<Text> lessonLocations = new ArrayList<>();
    private final List<Text> detailLines = new ArrayList<>();
    private final List<Text> noteTexts = new ArrayList<>();
    private final List<Text> followUpTexts = new ArrayList<>();
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
                .map(level -> STAGE_STYLE_CLASSES.get(SchoolingLevel.stageOf(level.toString())))
                .orElse(NO_STAGE_STYLE_CLASS);
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

    /**
     * Fills the details: school and contact details together, then the guardian and their follow-ups, lessons,
     * any tags and notes.
     */
    private void fillDetails(List<StudentCardText.LessonLine> lines) {
        VBox contactDetails = new VBox(16);
        person.getSchool().ifPresent(school -> {
            Text text = text(school.toString(), "row-value");
            detailLines.add(text);
            contactDetails.getChildren().add(section("School", text));
        });

        VBox contact = new VBox(3);
        for (String line : List.of(person.getPhone().value, person.getEmail().value, person.getAddress().value)) {
            Text text = text(line, "row-value");
            detailLines.add(text);
            contact.getChildren().add(text);
        }
        contactDetails.getChildren().add(section("Contact", contact));
        details.getChildren().add(contactDetails);

        VBox guardian = new VBox(3);
        if (person.getGuardian().isPresent()) {
            guardian.getChildren().addAll(text(person.getGuardian().get().getName().fullName, "row-value"),
                    text(person.getGuardian().get().getPhone().value, "row-secondary"));
        } else {
            guardian.getChildren().add(muted(StudentCardText.NOT_SET));
        }
        details.getChildren().add(section("Guardian", guardian));

        List<FollowUp> followUps = person.getFollowUps();
        Node followUpContent = followUps.isEmpty() ? muted(StudentCardText.NO_FOLLOW_UPS) : followUpTable(followUps);
        details.getChildren().add(section("Follow-ups", followUpContent));

        Node lessonContent = lines.isEmpty() ? muted(StudentCardText.NO_LESSONS) : lessonTable(lines);
        details.getChildren().add(section("Lessons", lessonContent));

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

        List<ProgressNote> notes = person.getNotes();
        Node noteContent = notes.isEmpty() ? muted(StudentCardText.NO_NOTES) : noteTable(notes);
        details.getChildren().add(section("Notes", noteContent));
    }

    /** Lays out the outstanding follow-ups in the order recorded: number and text. Numbers match the index. */
    private GridPane followUpTable(List<FollowUp> followUps) {
        GridPane table = new GridPane();
        table.getColumnConstraints().add(new ColumnConstraints(NOTE_COLUMN_WIDTHS[0]));
        table.setHgap(NOTE_COLUMN_GAP);
        table.setVgap(4);
        for (int i = 0; i < followUps.size(); i++) {
            Text number = text((i + 1) + ".", "lesson-number");
            Text followUpText = text(followUps.get(i).getText(), "row-value");
            followUpTexts.add(followUpText);
            // A long item wraps; its number stays level with its first line.
            GridPane.setValignment(number, VPos.TOP);
            table.addRow(i, number, followUpText);
        }
        return table;
    }

    /**
     * Lays out the progress notes oldest first: number, date and text in columns. Numbers match {@code NOTE_INDEX}.
     */
    private GridPane noteTable(List<ProgressNote> notes) {
        GridPane table = new GridPane();
        for (double width : NOTE_COLUMN_WIDTHS) {
            table.getColumnConstraints().add(new ColumnConstraints(width));
        }
        table.setHgap(NOTE_COLUMN_GAP);
        table.setVgap(4);
        for (int i = 0; i < notes.size(); i++) {
            ProgressNote note = notes.get(i);
            Text number = text((i + 1) + ".", "lesson-number");
            Text date = text(NOTE_DATE_FORMAT.format(note.getDate()), "row-secondary");
            Text noteText = text(note.getText(), "row-value");
            noteTexts.add(noteText);
            // A long note wraps; its number and date stay level with its first line.
            GridPane.setValignment(number, VPos.TOP);
            GridPane.setValignment(date, VPos.TOP);
            table.addRow(i, number, date, noteText);
        }
        return table;
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
            Text subject = text(line.subject(), "lesson-subject");
            subject.setWrappingWidth(LESSON_COLUMN_WIDTHS[1]);
            List<Text> cells = List.of(text(line.number() + ".", "lesson-number"), subject,
                    text(line.slot(), "row-value"), text(line.rate(), "row-value"));
            // A long subject wraps in its column; the other cells stay level with its first line.
            cells.forEach(cell -> GridPane.setValignment(cell, VPos.TOP));
            table.addRow(row++, cells.toArray(Node[]::new));
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
        // The name keeps most of the cell and its longest word, and the level keeps its longest word beside it.
        // When both cannot fit, the name keeps its share and the level the rest; neither leaves the cell.
        String nameText = name.getText();
        double nameWidth = textWidth(nameText, NAME_FONT_FAMILY, NAME_FONT_SIZE);
        double nameWord = longestWordWidth(nameText, NAME_FONT_FAMILY, NAME_FONT_SIZE);
        boolean hasLevel = schoolingLevel.isManaged();
        double levelWidth = hasLevel
                ? textWidth(schoolingLevel.getText(), BODY_FONT_FAMILY, LEVEL_FONT_SIZE) + LEVEL_GAP : 0;
        double levelFloor = hasLevel
                ? longestWordWidth(schoolingLevel.getText(), BODY_FONT_FAMILY, LEVEL_FONT_SIZE) + LEVEL_GAP : 0;
        DoubleExpression room = columns.name().subtract(BADGE_WIDTH);
        DoubleExpression nameLimit = Bindings.createDoubleBinding(() -> {
            double share = room.get() * NAME_SHARE;
            double wanted = Math.max(room.get() - levelWidth, Math.min(nameWidth, Math.max(share, nameWord)));
            return Math.min(wanted, Math.max(room.get() - levelFloor, share));
        }, room);
        wrapWhenWider(name, nameLimit, nameWidth);
        wrapWhenWider(schoolingLevel, room.subtract(LEVEL_GAP).subtract(Bindings.min(nameLimit, nameWidth)),
                levelWidth - LEVEL_GAP);
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
        detailLines.forEach(line -> wrapWhenLonger(line, contactLimit, BODY_FONT_SIZE));
        DoubleExpression noteLimit = Bindings.min(detailsWidth, NOTES_MAX_WIDTH)
                .subtract(Arrays.stream(NOTE_COLUMN_WIDTHS).sum() + NOTE_COLUMN_WIDTHS.length * NOTE_COLUMN_GAP);
        noteTexts.forEach(note -> wrapWhenLonger(note, noteLimit, BODY_FONT_SIZE));
        DoubleExpression followUpLimit = Bindings.min(detailsWidth, NOTES_MAX_WIDTH)
                .subtract(NOTE_COLUMN_WIDTHS[0] + NOTE_COLUMN_GAP);
        followUpTexts.forEach(followUp -> wrapWhenLonger(followUp, followUpLimit, BODY_FONT_SIZE));
    }

    /** Returns the style class that colours {@code level} by its school stage. */
    public static String stageStyleClass(String level) {
        return stageStyleClass(SchoolingLevel.stageOf(level));
    }

    /** Returns the style class that colours a school stage, e.g. "level-primary". */
    static String stageStyleClass(SchoolingLevel.Stage stage) {
        return STAGE_STYLE_CLASSES.get(stage);
    }

    private static VBox section(String caption, Node content) {
        Label label = new Label(caption);
        label.getStyleClass().add("detail-caption");
        return new VBox(8, label, content);
    }

    /** Fixes {@code cell} to {@code width}, so it lines up with its column. */
    static void size(Region cell, DoubleExpression width) {
        cell.minWidthProperty().bind(width);
        cell.prefWidthProperty().bind(width);
        cell.maxWidthProperty().bind(width);
    }

    /** Wraps {@code text}, in the body font at {@code fontSize}, as {@link #wrapWhenWider} does. */
    private static void wrapWhenLonger(Text text, DoubleExpression limit, double fontSize) {
        wrapWhenWider(text, limit, textWidth(text.getText(), BODY_FONT_FAMILY, fontSize));
    }

    /**
     * Wraps {@code text} at {@code limit} only when its {@code natural} width is wider than that.
     * A {@code Text} with a wrapping width always takes that whole width, which would spread short values apart.
     */
    private static void wrapWhenWider(Text text, DoubleExpression limit, double natural) {
        text.wrappingWidthProperty().bind(Bindings.createDoubleBinding(() ->
                natural > limit.get() ? limit.get() : 0, limit));
    }

    /** Returns the width of the widest word of {@code value}, measured as {@link #textWidth} does. */
    private static double longestWordWidth(String value, String family, double size) {
        return Arrays.stream(value.split(" ")).mapToDouble(word -> textWidth(word, family, size)).max().orElse(0);
    }

    /** Returns the width of {@code value} in a bundled font at {@code size}, as styled in LightTheme.css. */
    private static double textWidth(String value, String family, double size) {
        Text measure = new Text(value);
        measure.setFont(Font.font(family, size));
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
