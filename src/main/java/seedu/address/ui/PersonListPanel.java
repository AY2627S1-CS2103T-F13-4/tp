package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.SchoolingLevel.Stage;
import seedu.address.model.person.Student;

/**
 * Panel containing the table of students.
 */
public class PersonListPanel extends UiPart<Region> {
    /** Part of the visible height scrolled per page, leaving some overlap for context; also used by the week. */
    static final double PAGE_FRACTION = 0.85;

    private static final String FXML = "PersonListPanel.fxml";
    /** Most scrolls opening a student makes at once to put its row in place, so it can never loop. */
    private static final int ALIGN_ATTEMPTS = 4;
    /** Below this list width the Subjects and Guardian columns are hidden; the details still show them. */
    private static final double WIDE_TABLE_WIDTH = 900;
    /**
     * Width of the list not available to the columns: the # column, row padding, gaps between the visible
     * columns and the scroll bar. Narrow tables have two columns, so two gaps, fewer.
     */
    private static final double CHROME_WIDE = 26 + 5 * 20 + 32 + 14;
    private static final double CHROME_NARROW = 26 + 3 * 20 + 32 + 14;
    /** Width of the list not available to a row's details: row padding, the details' indent and the scroll bar. */
    private static final double DETAILS_CHROME_WIDTH = 100;
    private static final Map<Stage, String> STAGE_NAMES = Map.of(
            Stage.PRIMARY, "Primary", Stage.SECONDARY, "Secondary", Stage.JC, "JC", Stage.OTHER, "Other");

    /** Widths of the table's columns, shared by the header and every row. */
    record Columns(DoubleExpression name, DoubleExpression subjects, DoubleExpression lesson,
            DoubleExpression rate, DoubleExpression guardian, DoubleExpression details, BooleanExpression wide) {}

    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Student> personListView;
    @FXML
    private Label listTitle;
    @FXML
    private Label listSummary;
    @FXML
    private Label nameHeading;
    @FXML
    private Label subjectsHeading;
    @FXML
    private Label lessonHeading;
    @FXML
    private Label rateHeading;
    @FXML
    private Label guardianHeading;

    private Runnable onAddStudent = () -> { };

    @FXML
    private Label emptyTitle;
    @FXML
    private Label emptyDetail;
    @FXML
    private Label emptyCommand;
    private final Columns columns;
    private final boolean archived;
    /** The student whose details were open just before the list last changed. */
    private Student openBeforeChange;

    /**
     * Creates a {@code PersonListPanel} that shows {@code personList}, the list that commands' indices refer to.
     * {@code allPersons} is the unfiltered list, used only to count students and choose the empty-state message.
     */
    public PersonListPanel(ObservableList<Student> personList, ObservableList<Student> allPersons) {
        this(personList, allPersons, false);
    }

    /** Creates the same student table for either active or archived students. */
    public PersonListPanel(ObservableList<Student> personList, ObservableList<Student> allPersons, boolean archived) {
        super(FXML);
        this.archived = archived;
        listTitle.setText(archived ? "Archived students" : "Students");
        ReadOnlyDoubleProperty listWidth = personListView.widthProperty();
        BooleanExpression wide = listWidth.greaterThanOrEqualTo(WIDE_TABLE_WIDTH);
        columns = new Columns(column(listWidth, 0.30, 0.50), column(listWidth, 0.22, 0),
                column(listWidth, 0.20, 0.34), column(listWidth, 0.08, 0.16), column(listWidth, 0.20, 0),
                listWidth.subtract(DETAILS_CHROME_WIDTH), wide);
        PersonCard.size(nameHeading, columns.name());
        PersonCard.size(subjectsHeading, columns.subjects());
        PersonCard.size(lessonHeading, columns.lesson());
        PersonCard.size(rateHeading, columns.rate());
        PersonCard.size(guardianHeading, columns.guardian());
        for (Label optional : new Label[] {subjectsHeading, guardianHeading}) {
            optional.visibleProperty().bind(wide);
            optional.managedProperty().bind(wide);
        }

        // Added before setItems, so it runs before the list view moves its selection for the same change.
        personList.addListener((ListChangeListener<Student>) change ->
                openBeforeChange = personListView.getSelectionModel().getSelectedItem());
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());

        emptyCommand.setStyle(MonospaceFont.style());

        personList.addListener(this::followOpenStudent);
        ListChangeListener<Student> refresh = change -> {
            updateStatus(personList.size(), allPersons.size());
            openOnlyRow();
            // Group headings count their students, so a change in one row can change another row's heading.
            // Rows whose student, number and heading are unchanged keep their card (see PersonListViewCell).
            personListView.refresh();
        };
        personList.addListener(refresh);
        allPersons.addListener(refresh);
        // When the shown rows change (after list, find, add or delete), fade them in to mark it.
        personList.addListener((ListChangeListener<Student>) change -> Motion.fadeIn(personListView, 0.4));
        updateStatus(personList.size(), allPersons.size());
        openOnlyRow();
    }

    /**
     * Opens the details of the student after (1) or before (-1) the open one, or the first or last if none is open,
     * scrolling only if that row is out of view. Lets keyboard users browse details from the command box.
     */
    public void moveDetails(int direction) {
        int size = personListView.getItems().size();
        if (size == 0) {
            return;
        }
        int current = personListView.getSelectionModel().getSelectedIndex();
        openStudent(current < 0 ? (direction > 0 ? 0 : size - 1) : Math.clamp(current + direction, 0, size - 1));
    }

    /**
     * Opens the details of the student at {@code zeroBasedIndex} in the list, scrolling to it, for {@code view}.
     */
    public void openStudent(int zeroBasedIndex) {
        personListView.getSelectionModel().select(zeroBasedIndex);
        withFlow(flow -> {
            flow.scrollTo(zeroBasedIndex);
            afterNextLayout(() -> showOpenRow(flow, zeroBasedIndex));
            // The details grow open over a short animation, so check again once they have their full height.
            Motion.afterResize(() -> afterNextLayout(() -> showOpenRow(flow, zeroBasedIndex)));
        });
    }

    /**
     * Scrolls the open row at {@code index} just into view, or, if it is taller than the list, lines up its top with
     * the list's top, where {@code scrollTo} would line up its bottom and hide the student's name.
     * The list places rows from estimated heights, which go stale as rows open and close, so this measures where the
     * row really is after each scroll and corrects again, at most {@code ALIGN_ATTEMPTS} times.
     */
    private <T extends IndexedCell<?>> void showOpenRow(VirtualFlow<T> flow, int index) {
        if (personListView.getSelectionModel().getSelectedIndex() != index) {
            return;
        }
        for (int attempt = 0; attempt < ALIGN_ATTEMPTS; attempt++) {
            T cell = flow.getVisibleCell(index);
            if (cell == null) {
                flow.scrollTo(index);
            } else if (cell.getHeight() > flow.getHeight()) {
                if (Math.abs(cell.getLayoutY()) < 1) {
                    return;
                }
                flow.scrollToTop(cell);
            } else {
                if (cell.getLayoutY() > -1 && cell.getLayoutY() + cell.getHeight() < flow.getHeight() + 1) {
                    return;
                }
                flow.scrollTo(cell);
            }
            // Lay the rows out now: after a scroll the list rebuilds them from its estimates, possibly elsewhere,
            // and leaving that to the next frame could show the wrong rows or none.
            flow.requestLayout();
            flow.layout();
        }
    }

    /** Runs {@code action} once, right after the window next lays itself out, when rows have their real heights. */
    private void afterNextLayout(Runnable action) {
        Scene scene = personListView.getScene();
        scene.addPostLayoutPulseListener(new Runnable() {
            @Override
            public void run() {
                scene.removePostLayoutPulseListener(this);
                action.run();
            }
        });
        Platform.requestNextPulse();
    }

    /** Sets what the Add student button does. */
    public void setOnAddStudent(Runnable onAddStudent) {
        this.onAddStudent = onAddStudent;
    }

    @FXML
    private void handleAddStudent() {
        onAddStudent.run();
    }

    /** Closes any open student details. */
    public void closeDetails() {
        personListView.getSelectionModel().clearSelection();
    }

    /**
     * Scrolls the list by most of one screen: up for a negative {@code direction}, down otherwise.
     * Lets keyboard users page through the list while focus stays in the command box.
     */
    public void scrollPage(int direction) {
        withFlow(flow -> flow.scrollPixels(Math.signum(direction) * flow.getHeight() * PAGE_FRACTION));
    }

    /** Runs {@code action} on the list's scrolling area, which exists once the list has been shown. */
    private void withFlow(Consumer<VirtualFlow<?>> action) {
        VirtualFlow<?> flow = (VirtualFlow<?>) personListView.lookup(".virtual-flow");
        if (flow != null) {
            action.accept(flow);
        }
    }

    /**
     * Keeps the open details on the same student when the list changes. The list view keeps the selected row
     * number, so without this, deleting the open student would open whoever takes their row.
     * An edited student is a new object, so if the open one is gone, the details follow the one student the change
     * added in its place; a delete adds no one, so the details close.
     */
    private void followOpenStudent(ListChangeListener.Change<? extends Student> change) {
        Student open = openBeforeChange;
        if (open == null || personListView.getItems().stream().anyMatch(student -> student == open)) {
            return;
        }
        List<Student> added = new ArrayList<>();
        while (change.next()) {
            added.addAll(change.getAddedSubList());
        }
        if (added.size() == 1) {
            openStudent(personListView.getItems().indexOf(added.get(0)));
        } else {
            closeDetails();
        }
    }

    /** Opens the only row when a search leaves one student, and closes details whose row has gone. */
    private void openOnlyRow() {
        int size = personListView.getItems().size();
        if (size == 1) {
            personListView.getSelectionModel().select(0);
        } else if (personListView.getSelectionModel().getSelectedIndex() >= size) {
            personListView.getSelectionModel().clearSelection();
        }
    }

    /** Returns a column's width: its share of the list's free width, which depends on whether the table is wide. */
    private static DoubleExpression column(ReadOnlyDoubleProperty listWidth, double wideShare, double narrowShare) {
        return Bindings.createDoubleBinding(() -> {
            boolean wide = listWidth.get() >= WIDE_TABLE_WIDTH;
            double free = Math.max(0, listWidth.get() - (wide ? CHROME_WIDE : CHROME_NARROW));
            return free * (wide ? wideShare : narrowShare);
        }, listWidth);
    }

    private void updateStatus(int shown, int total) {
        listSummary.setText(StudentListStatus.summary(shown, total));
        emptyTitle.setText(StudentListStatus.emptyTitle(total, archived));
        emptyDetail.setText(StudentListStatus.emptyDetail(total, archived));
        emptyCommand.setText(StudentListStatus.emptyCommand(total, archived));
    }

    /** Returns a group heading such as "Primary 2": a dot in the stage's colour, its name and how many it has. */
    private static HBox groupHeading(Stage stage, long size) {
        Region dot = new Region();
        dot.getStyleClass().addAll("nav-dot", PersonCard.stageStyleClass(stage));
        Label name = new Label(STAGE_NAMES.get(stage));
        name.getStyleClass().add("group-name");
        Label count = new Label(String.valueOf(size));
        count.getStyleClass().add("group-count");
        HBox heading = new HBox(10, dot, name, count);
        heading.setAlignment(Pos.CENTER_LEFT);
        heading.getStyleClass().add("group-heading");
        return heading;
    }

    /**
     * Custom {@code ListCell} that displays a {@code Student} as a {@code PersonCard} row.
     * A selected row shows its details; selecting another only toggles those two rows.
     */
    class PersonListViewCell extends ListCell<Student> {
        private PersonCard card;
        private Student shownPerson;
        private int shownIndex = -1;
        private String shownHeading = "";

        PersonListViewCell() {
            // Clicking the open row closes it, the mouse version of Esc.
            addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (!isEmpty() && isSelected()) {
                    getListView().getSelectionModel().clearSelection();
                    event.consume();
                }
            });
        }

        @Override
        protected void updateItem(Student person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                card = null;
                shownPerson = null;
                shownIndex = -1;
                shownHeading = "";
                setGraphic(null);
                setText(null);
            } else {
                // The first student of each school stage gets a heading, e.g. "Primary 2". The model lists students
                // by stage, so each stage is one run of rows.
                List<Student> shown = getListView().getItems();
                Stage stage = person.getStage();
                boolean startsGroup = getIndex() == 0 || shown.get(getIndex() - 1).getStage() != stage;
                long groupSize = startsGroup
                        ? shown.stream().filter(student -> student.getStage() == stage).count() : 0;
                String heading = startsGroup ? STAGE_NAMES.get(stage) + " " + groupSize : "";
                if (person == shownPerson && getIndex() == shownIndex && heading.equals(shownHeading)) {
                    // The list also redraws a row that has not changed, e.g. right after it opens.
                    // Keeping its card then lets an open or close animation finish.
                    return;
                }
                card = new PersonCard(person, getIndex() + 1);
                card.bindColumns(columns);
                // Read the selection for this index: the cell's own flag may still be for the row it showed before.
                card.setOpen(getListView().getSelectionModel().isSelected(getIndex()));
                shownPerson = person;
                shownIndex = getIndex();
                shownHeading = heading;
                setGraphic(startsGroup ? new VBox(groupHeading(stage, groupSize), card.getRoot()) : card.getRoot());
            }
        }

        @Override
        public void updateSelected(boolean selected) {
            super.updateSelected(selected);
            if (card != null) {
                // A row redrawn while scrolling already shows its state, so only a real change animates.
                card.animateOpen(selected);
            }
        }
    }

}
