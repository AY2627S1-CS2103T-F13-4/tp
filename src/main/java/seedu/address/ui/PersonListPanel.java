package seedu.address.ui;

import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Student;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    /** Width of the list taken by the # column, row padding, the gaps between columns and the scroll bar. */
    private static final double ROW_CHROME_WIDTH = 140;
    /** Below this list width the Guardian column is hidden; the guardian still shows in a row's details. */
    private static final double WIDE_TABLE_WIDTH = 900;
    /** Shares of the free width for the Next lesson and Guardian columns; the Student column takes the rest. */
    private static final double LESSON_SHARE_WIDE = 0.36;
    private static final double LESSON_SHARE_NARROW = 0.45;
    private static final double GUARDIAN_SHARE_WIDE = 0.22;
    /** The Next lesson column is never narrower than a next-lesson line, e.g. "H2 Chemistry  Sat 1.00pm  +2 more". */
    private static final double MIN_LESSON_WIDTH = 230;
    /** Width of the list not available to a row's details: row padding, the details' indent and the scroll bar. */
    private static final double DETAILS_CHROME_WIDTH = 100;
    /** Part of the visible height scrolled per page, leaving some overlap for context. */
    private static final double PAGE_FRACTION = 0.85;

    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Student> personListView;
    @FXML
    private Label listSummary;
    @FXML
    private Label studentHeading;
    @FXML
    private Label lessonsHeading;
    @FXML
    private Label guardianHeading;

    private final BooleanBinding wide;
    private final DoubleBinding studentWidth;
    private final DoubleBinding lessonsWidth;
    private final DoubleBinding guardianWidth;
    private final DoubleBinding detailsWidth;

    private final Label emptyTitle = new Label();
    private final Label emptyDetail = new Label();

    /**
     * Creates a {@code PersonListPanel} that shows {@code personList}, the list that commands' indices refer to.
     * {@code allPersons} is the unfiltered list, used only to count students and choose the empty-state message.
     */
    public PersonListPanel(ObservableList<Student> personList, ObservableList<Student> allPersons) {
        super(FXML);
        ReadOnlyDoubleProperty listWidth = personListView.widthProperty();
        wide = listWidth.greaterThanOrEqualTo(WIDE_TABLE_WIDTH);
        lessonsWidth = Bindings.createDoubleBinding(() -> Math.max(MIN_LESSON_WIDTH,
                free(listWidth) * (isWide(listWidth) ? LESSON_SHARE_WIDE : LESSON_SHARE_NARROW)), listWidth);
        detailsWidth = listWidth.subtract(DETAILS_CHROME_WIDTH);
        guardianWidth = Bindings.createDoubleBinding(() ->
                isWide(listWidth) ? free(listWidth) * GUARDIAN_SHARE_WIDE : 0, listWidth);
        studentWidth = Bindings.createDoubleBinding(() ->
                Math.max(0, free(listWidth) - lessonsWidth.get() - guardianWidth.get()), listWidth);
        sizeHeading(studentHeading, studentWidth);
        sizeHeading(lessonsHeading, lessonsWidth);
        sizeHeading(guardianHeading, guardianWidth);
        guardianHeading.visibleProperty().bind(wide);
        guardianHeading.managedProperty().bind(wide);

        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        // A clicked row, or the only row left after a search, expands to show its details.
        personListView.getSelectionModel().selectedIndexProperty().addListener((unused1, unused2, unused3) ->
                personListView.refresh());

        emptyTitle.getStyleClass().add("empty-title");
        emptyDetail.getStyleClass().add("empty-detail");
        emptyDetail.setWrapText(true);
        VBox emptyState = new VBox(emptyTitle, emptyDetail);
        emptyState.getStyleClass().add("empty-state");
        personListView.setPlaceholder(emptyState);

        ListChangeListener<Student> refresh = change -> {
            updateStatus(personList.size(), allPersons.size());
            personListView.refresh();
        };
        personList.addListener(refresh);
        allPersons.addListener(refresh);
        updateStatus(personList.size(), allPersons.size());
    }

    /**
     * Scrolls the list by most of one screen: up for a negative {@code direction}, down otherwise.
     * Lets keyboard users page through the list while focus stays in the command box.
     */
    public void scrollPage(int direction) {
        VirtualFlow<?> flow = (VirtualFlow<?>) personListView.lookup(".virtual-flow");
        if (flow != null) {
            flow.scrollPixels(Math.signum(direction) * flow.getHeight() * PAGE_FRACTION);
        }
    }

    /**
     * Opens the details of the student after (1) or before (-1) the open one, or the first or last if none is open,
     * and scrolls to it. Lets keyboard users browse details while focus stays in the command box.
     */
    public void moveDetails(int direction) {
        int size = personListView.getItems().size();
        if (size == 0) {
            return;
        }
        int current = personListView.getSelectionModel().getSelectedIndex();
        int next = current < 0 ? (direction > 0 ? 0 : size - 1) : Math.max(0, Math.min(size - 1, current + direction));
        personListView.getSelectionModel().select(next);
        personListView.scrollTo(next);
    }

    /** Closes any open student details. */
    public void closeDetails() {
        personListView.getSelectionModel().clearSelection();
    }

    /** Returns the list width left for the Student, Lessons and Guardian columns. */
    private static double free(ReadOnlyDoubleProperty listWidth) {
        return Math.max(0, listWidth.get() - ROW_CHROME_WIDTH);
    }

    private static boolean isWide(ReadOnlyDoubleProperty listWidth) {
        return listWidth.get() >= WIDE_TABLE_WIDTH;
    }

    private static void sizeHeading(Label heading, DoubleBinding width) {
        heading.minWidthProperty().bind(width);
        heading.prefWidthProperty().bind(width);
        heading.maxWidthProperty().bind(width);
    }

    private void updateStatus(int shown, int total) {
        listSummary.setText(StudentListStatus.summary(shown, total));
        emptyTitle.setText(StudentListStatus.emptyTitle(total));
        emptyDetail.setText(StudentListStatus.emptyDetail(total));
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Student} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Student> {
        @Override
        protected void updateItem(Student person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                boolean expanded = getListView().getItems().size() == 1 || isSelected();
                PersonCard card = new PersonCard(person, getIndex() + 1, expanded);
                card.bindColumns(studentWidth, lessonsWidth, guardianWidth, wide, detailsWidth);
                setGraphic(card.getRoot());
            }
        }
    }

}
